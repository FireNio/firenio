/*
 * Copyright 2015 The Baseio Project
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.firenio.baseio.component;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.firenio.baseio.TimeoutException;
import com.firenio.baseio.collection.DelayedQueue.DelayTask;
import com.firenio.baseio.common.Assert;
import com.firenio.baseio.common.Util;
import com.firenio.baseio.concurrent.Callback;
import com.firenio.baseio.concurrent.Waiter;

/**
 * @author wangkai
 *
 */
public final class ChannelConnector extends ChannelContext implements Closeable {

    private volatile Callback<Channel> callback;
    private volatile boolean           callbacked = true;
    private Channel                    ch;
    private NioEventLoop               eventLoop;
    private SocketChannel              javaChannel;
    private volatile DelayTask         timeoutTask;

    public ChannelConnector(int port) {
        this("127.0.0.1", port);
    }

    public ChannelConnector(NioEventLoop eventLoop, String host, int port) {
        this(eventLoop.getGroup(), host, port);
        this.eventLoop = eventLoop;
    }

    public ChannelConnector(NioEventLoopGroup group) {
        this(group, "127.0.0.1", 0);
    }

    public ChannelConnector(NioEventLoopGroup group, String host, int port) {
        super(group, host, port);
        if (!group.isSharable() && !group.isRunning()) {
            group.setEventLoopSize(1);
        }
    }

    public ChannelConnector(String host, int port) {
        this(new NioEventLoopGroup(1), host, port);
    }

    @Override
    protected void channelEstablish(Channel ch, Throwable ex) {
        if (!callbacked) {
            this.ch = ch;
            this.callbacked = true;
            this.timeoutTask.cancel();
            try {
                this.callback.call(ch, ex);
            } catch (Throwable e) {
                if (e instanceof Error) {
                    e.printStackTrace(System.err);
                }
            }
        }
    }

    @Override
    public synchronized void close() throws IOException {
        Util.close(ch);
        Util.close(javaChannel);
        Util.stop(this);
        if (!getProcessorGroup().isSharable()) {
            this.eventLoop = null;
        }
        this.ch = null;
    }

    public synchronized Channel connect() throws Exception {
        return connect(3000);
    }

    public synchronized void connect(Callback<Channel> callback) throws Exception {
        connect(callback, 3000);
    }

    public synchronized void connect(Callback<Channel> callback, long timeout) throws Exception {
        Assert.notNull(callback, "null callback");
        if (isConnected()) {
            callback.call(ch, null);
            return;
        }
        if (!callbacked) {
            throw new IOException("connect is pending");
        }
        this.callbacked = false;
        this.timeoutTask = new TimeoutTask(timeout);
        this.callback = callback;
        this.getProcessorGroup().setContext(this);
        Util.start(getProcessorGroup());
        Util.start(this);
        if (eventLoop == null) {
            eventLoop = getProcessorGroup().getNext();
        }
        this.javaChannel = SocketChannel.open();
        this.javaChannel.configureBlocking(false);
        boolean submitted = this.eventLoop.submit(new Runnable() {

            @Override
            public void run() {
                try {
                    if (!javaChannel.connect(getServerAddress())) {
                        eventLoop.registSelector(ChannelConnector.this, SelectionKey.OP_CONNECT);
                        eventLoop.schedule(timeoutTask);
                    }
                } catch (Throwable e) {
                    channelEstablish(null, e);
                }
            }
        });
        if (!submitted) {
            channelEstablish(null, new IOException("task submit failed"));
        }
    }

    public synchronized Channel connect(long timeout) throws Exception {
        Waiter<Channel> callback = new Waiter<>();
        this.connect(callback, timeout);
        if (eventLoop.inEventLoop()) {
            throw new IOException("can not blocking connect in its event loop");
        }
        // If your application blocking here, check if you are blocking the io thread.
        // Notice that do not blocking io thread at any time.
        if (callback.await()) {
            Util.close(this);
            throw new TimeoutException("connect to " + getServerAddress() + " time out");
        }
        if (callback.isFailed()) {
            Util.close(this);
            Throwable ex = callback.getThrowable();
            if (ex instanceof Exception) {
                throw (Exception) callback.getThrowable();
            }
            throw new IOException("connect failed", ex);
        }
        return getChannel();
    }

    public Channel getChannel() {
        return ch;
    }

    public NioEventLoop getEventLoop() {
        return eventLoop;
    }

    @Override
    public SocketChannel getSelectableChannel() {
        return javaChannel;
    }

    @Override
    public boolean isActive() {
        return isConnected();
    }

    public boolean isConnected() {
        Channel ch = this.ch;
        return ch != null && ch.isOpened();
    }

    @Override
    public String toString() {
        Channel ch = this.ch;
        if (ch == null) {
            return super.toString();
        }
        return ch.toString();
    }

    class TimeoutTask extends DelayTask {

        public TimeoutTask(long delay) {
            super(delay);
        }

        @Override
        @SuppressWarnings("resource")
        public void run() {
            setDone(true);
            ChannelConnector connector = ChannelConnector.this;
            NioEventLoop eventLoop = connector.eventLoop;
            if (eventLoop != null) {
                eventLoop.cancelSelectionKey(connector.javaChannel);
            }
            connector.channelEstablish(null, new TimeoutException("connect timeout"));
        }

    }

}

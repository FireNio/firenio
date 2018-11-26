/*
 * Copyright 2015-2017 GenerallyCloud.com
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
package com.generallycloud.baseio.component;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.generallycloud.baseio.LifeCycleUtil;
import com.generallycloud.baseio.TimeoutException;
import com.generallycloud.baseio.common.Assert;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.concurrent.Callback;
import com.generallycloud.baseio.concurrent.Waiter;

/**
 * @author wangkai
 *
 */
public class ChannelConnector extends ChannelContext implements Closeable {

    private NioSocketChannel                    ch;
    private NioEventLoop                        eventLoop;
    private SocketChannel                       javaChannel;
    private volatile Callback<NioSocketChannel> callback;

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
        group.setEventLoopSize(1);
    }

    public ChannelConnector(String host, int port) {
        this(new NioEventLoopGroup(), host, port);
    }

    @Override
    public synchronized void close() throws IOException {
        CloseUtil.close(ch);
        CloseUtil.close(javaChannel);
        LifeCycleUtil.stop(this);
        if (!getProcessorGroup().isSharable()) {
            this.eventLoop = null;
        }
        this.ch = null;
    }

    public synchronized NioSocketChannel connect() throws IOException {
        return connect(3000);
    }

    public synchronized NioSocketChannel connect(long timeout) throws IOException {
        ConnectCallback<NioSocketChannel> callback = new ConnectCallback<>();
        this.connect(callback);
        if (eventLoop.inEventLoop()) {
            throw new IOException("can not blocking connect in its event loop");
        }
        if (callback.await(timeout)) {
            CloseUtil.close(this);
            throw new TimeoutException("connect to " + getServerAddress() + " time out");
        }
        if (callback.isFailed()) {
            CloseUtil.close(this);
            Throwable ex = callback.getThrowable();
            String errorMsg = "connect to " + getServerAddress() + " failed, nested exception is "
                    + ex.getMessage();
            throw new IOException(errorMsg, ex);
        }
        return getChannel();
    }

    public synchronized void connect(Callback<NioSocketChannel> callback) throws IOException {
        Assert.notNull(callback, "null callback");
        if (isConnected()) {
            callback.call(ch, null);
            return;
        }
        this.callback = callback;
        this.getProcessorGroup().setContext(this);
        LifeCycleUtil.start(getProcessorGroup());
        LifeCycleUtil.start(this);
        if (eventLoop == null) {
            eventLoop = getProcessorGroup().getNext();
        }
        this.javaChannel = SocketChannel.open();
        this.javaChannel.configureBlocking(false);
        this.eventLoop.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    if (!javaChannel.connect(getServerAddress())) {
                        eventLoop.registSelector(ChannelConnector.this, SelectionKey.OP_CONNECT);
                    }
                } catch (IOException e) {
                    finishConnect(null, e);
                }
            }
        });
    }

    protected void finishConnect(NioSocketChannel ch, Throwable exception) {
        this.ch = ch;
        this.callback.call(ch, exception);
    }

    public NioSocketChannel getChannel() {
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
        NioSocketChannel ch = this.ch;
        return ch != null && ch.isOpened();
    }

    @Override
    public String toString() {
        NioSocketChannel ch = this.ch;
        if (ch == null) {
            return super.toString();
        }
        return ch.toString();
    }

    class ConnectCallback<T> extends Waiter<T> {
        
        @Override
        public void call(T res, Throwable ex) {
            synchronized (this) {
                this.isDnoe = true;
                this.response = res;
                this.throwable = ex;
                this.notify();
                if (isTimeouted()) {
                    CloseUtil.close((Closeable) res);
                }
            }
        }
    }

}

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
import java.net.InetAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.firenio.baseio.Develop;
import com.firenio.baseio.TimeoutException;
import com.firenio.baseio.collection.DelayedQueue.DelayTask;
import com.firenio.baseio.common.Assert;
import com.firenio.baseio.common.Util;
import com.firenio.baseio.component.NioEventLoop.EpollNioEventLoopUnsafe;
import com.firenio.baseio.component.NioEventLoop.JavaNioEventLoopUnsafe;
import com.firenio.baseio.concurrent.Callback;
import com.firenio.baseio.concurrent.Waiter;
import com.firenio.baseio.log.Logger;
import com.firenio.baseio.log.LoggerFactory;

/**
 * @author wangkai
 *
 */
public final class ChannelConnector extends ChannelContext implements Closeable {

    private volatile Callback<Channel> callback;
    private volatile boolean           callbacked = true;
    private Channel                    ch;
    private NioEventLoop               eventLoop;
    private volatile DelayTask         timeoutTask;
    private ConnectorUnsafe            unsafe;
    private static final Logger        logger     = LoggerFactory.getLogger(ChannelConnector.class);

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
        if (Native.EPOLL_AVAIABLE) {
            unsafe = new EpollConnectorUnsafe();
        } else {
            unsafe = new JavaConnectorUnsafe();
        }
    }

    public ChannelConnector(String host, int port) {
        this(new NioEventLoopGroup(1), host, port);
    }

    @Override
    protected void channelEstablish(Channel ch, Throwable ex) {
        if (!callbacked) {
            this.unsafe.channelEstablish(ch, eventLoop, ex);
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
        this.timeoutTask = new TimeoutTask(this, timeout);
        this.callback = callback;
        this.getProcessorGroup().setContext(this);
        Util.start(getProcessorGroup());
        Util.start(this);
        if (eventLoop == null) {
            eventLoop = getProcessorGroup().getNext();
        }
        boolean submitted = this.eventLoop.submit(new Runnable() {

            @Override
            public void run() {
                try {
                    ChannelConnector ctx = ChannelConnector.this;
                    unsafe.connect(ctx, ctx.eventLoop);
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

    protected ConnectorUnsafe getUnsafe() {
        return unsafe;
    }

    @Override
    public boolean isActive() {
        return isConnected();
    }

    public boolean isConnected() {
        Channel ch = this.ch;
        return ch != null && ch.isOpen();
    }

    @Override
    public String toString() {
        Channel ch = this.ch;
        if (ch == null) {
            return super.toString();
        }
        return ch.toString();
    }

    static abstract class ConnectorUnsafe {

        abstract void connect(ChannelConnector ctx, NioEventLoop el) throws IOException;

        abstract void channelEstablish(Channel ch, NioEventLoop el, Throwable ex);

    }

    static final class EpollConnectorUnsafe extends ConnectorUnsafe {

        private int    fd = -1;
        private String remoteAddr;

        @Override
        void connect(ChannelConnector ctx, NioEventLoop el) throws IOException {
            EpollNioEventLoopUnsafe un = (EpollNioEventLoopUnsafe) el.getUnsafe();
            InetAddress host = InetAddress.getByName(ctx.getHost());
            this.remoteAddr = host.getHostAddress();
            int fd = Native.connect(host.getHostAddress(), ctx.getPort());
            Native.throwException(fd);
            this.fd = fd;
            el.schedule(ctx.timeoutTask);
            un.ctxs.put(fd, ctx);
            int res = Native.epoll_add(un.epfd, fd, Native.EPOLLOUT);
            Native.throwException(res);
        }

        @Override
        void channelEstablish(Channel ch, NioEventLoop el, Throwable ex) {
            if (ex != null && fd != -1) {
                EpollNioEventLoopUnsafe un = (EpollNioEventLoopUnsafe) el.getUnsafe();
                un.ctxs.remove(fd);
                if (Develop.NATIVE_DEBUG) {
                    int res = Native.epoll_del(un.epfd, fd);
                    if (res == -1) {
                        logger.error("cancel...epfd:{},fd:{}", un.epfd, fd);
                    }
                    res = Native.close(fd);
                    if (res == -1) {
                        logger.error("cancel...fd:{}", fd);
                    }
                } else {
                    Native.epoll_del(un.epfd, fd);
                    Native.close(fd);
                }
                this.fd = -1;
            }
        }

        String getRemoteAddr() {
            return remoteAddr;
        }

        int getFd() {
            return fd;
        }

    }

    static final class JavaConnectorUnsafe extends ConnectorUnsafe {

        private SocketChannel javaChannel;

        @Override
        void connect(ChannelConnector ctx, NioEventLoop el) throws IOException {
            Util.close(javaChannel);
            this.javaChannel = SocketChannel.open();
            this.javaChannel.configureBlocking(false);
            if (!javaChannel.connect(ctx.getServerAddress())) {
                el.schedule(ctx.timeoutTask);
                JavaNioEventLoopUnsafe elUnsafe = (JavaNioEventLoopUnsafe) el.getUnsafe();
                javaChannel.register(elUnsafe.getSelector(), SelectionKey.OP_CONNECT, ctx);
            }
        }

        SocketChannel getSelectableChannel() {
            return javaChannel;
        }

        @Override
        void channelEstablish(Channel ch, NioEventLoop el, Throwable ex) {
            final SocketChannel channel = this.javaChannel;
            if (ex != null && channel != null) {
                final JavaNioEventLoopUnsafe un = (JavaNioEventLoopUnsafe) el.getUnsafe();
                SelectionKey key = channel.keyFor(un.getSelector());
                if (key != null) {
                    key.cancel();
                }
                Util.close(channel);
                this.javaChannel = null;
            }
        }

    }

    static final class TimeoutTask extends DelayTask {

        private ChannelConnector ctx;

        public TimeoutTask(ChannelConnector ctx, long delay) {
            super(delay);
            this.ctx = ctx;
        }

        @Override
        public void run() {
            ctx.channelEstablish(null, new TimeoutException("connect timeout"));
        }
    }

}

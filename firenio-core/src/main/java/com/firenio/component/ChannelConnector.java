/*
 * Copyright 2015 The FireNio Project
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
package com.firenio.component;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import com.firenio.Develop;
import com.firenio.TimeoutException;
import com.firenio.collection.DelayedQueue;
import com.firenio.common.Assert;
import com.firenio.common.Util;
import com.firenio.concurrent.Callback;
import com.firenio.concurrent.Waiter;
import com.firenio.log.Logger;
import com.firenio.log.LoggerFactory;

/**
 * @author wangkai
 */
public final class ChannelConnector extends ChannelContext implements Closeable {

    private static final Logger                 logger      = LoggerFactory.getLogger(ChannelConnector.class);
    private              Channel                ch;
    private              NioEventLoop           eventLoop;
    private              ConnectorUnsafe        unsafe;
    private volatile     Callback<Channel>      callback;
    private volatile     boolean                callback_ed = true;
    private volatile     DelayedQueue.DelayTask timeoutTask;

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
        if (!callback_ed) {
            this.ch = ch;
            this.callback_ed = true;
            this.unsafe.channelEstablish(ch, ex);
            this.timeoutTask.cancel();
            try {
                this.callback.call(ch, ex);
            } catch (Throwable e) {
                logger.error(e);
            }
        }
    }

    @Override
    public synchronized void close() {
        Util.close(ch);
        Util.stop(this);
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
        if (!callback_ed) {
            throw new IOException("connect is pending");
        }
        this.callback_ed = false;
        this.callback = callback;
        this.timeoutTask = new TimeoutTask(this, timeout);
        this.getProcessorGroup().setContext(this);
        if (Native.EPOLL_AVAILABLE) {
            unsafe = new EpollConnectorUnsafe();
        } else {
            unsafe = new JavaConnectorUnsafe();
        }
        Util.start(getProcessorGroup());
        Util.start(this);
        if (eventLoop == null || !eventLoop.isRunning()) {
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
        Waiter<Channel> callback = new Waiter<>(this);
        this.connect(callback, timeout);
        if (eventLoop.inEventLoop()) {
            throw new IOException("can not blocking connect in its event loop");
        }
        // If your application blocking here, check if you are blocking the io thread.
        // Notice that do not blocking io thread at any time.
        callback.await();
        if (callback.isFailed()) {
            Util.close(this);
            throw new IOException("connect failed", callback.getThrowable());
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

        abstract void channelEstablish(Channel ch, Throwable ex);

    }

    static final class EpollConnectorUnsafe extends ConnectorUnsafe {

        private int            fd = -1;
        private String         remoteAddr;
        private EpollEventLoop el;

        @Override
        void connect(ChannelConnector ctx, NioEventLoop el) throws IOException {
            EpollEventLoop un   = (EpollEventLoop) el;
            InetAddress    host = InetAddress.getByName(ctx.getHost());
            this.remoteAddr = host.getHostAddress();
            int fd = Native.connect(host.getHostAddress(), ctx.getPort());
            Native.throwException(fd);
            this.fd = fd;
            this.el = un;
            un.ctxs.put(fd, ctx);
            el.schedule(ctx.timeoutTask);
            int res = Native.epoll_add(un.epfd, fd, Native.EPOLL_OUT);
            Native.throwException(res);
        }

        @Override
        void channelEstablish(Channel ch, Throwable ex) {
            if (ex != null && fd != -1) {
                el.ctxs.remove(fd);
                if (Develop.NATIVE_DEBUG) {
                    int res = Native.epoll_del(el.epfd, fd);
                    if (res == -1) {
                        logger.error("cancel...epfd:{},fd:{}", el.epfd, fd);
                    }
                    res = Native.close(fd);
                    if (res == -1) {
                        logger.error("cancel...fd:{}", fd);
                    }
                } else {
                    Native.epoll_del(el.epfd, fd);
                    Native.close(fd);
                }
                this.fd = -1;
            }
        }

        String getRemoteAddr() {
            return remoteAddr;
        }

    }

    static final class JavaConnectorUnsafe extends ConnectorUnsafe {

        private SocketChannel javaChannel;
        private SelectionKey  selectionKey;

        @Override
        void connect(ChannelConnector ctx, NioEventLoop el) throws IOException {
            this.javaChannel = SocketChannel.open();
            this.javaChannel.configureBlocking(false);
            if (!javaChannel.connect(ctx.getServerAddress())) {
                el.schedule(ctx.timeoutTask);
                JavaEventLoop elUnsafe = (JavaEventLoop) el;
                Selector      selector = elUnsafe.getSelector();
                this.selectionKey = javaChannel.register(selector, SelectionKey.OP_CONNECT, ctx);
            }
        }

        SocketChannel getSelectableChannel() {
            return javaChannel;
        }

        @Override
        void channelEstablish(Channel ch, Throwable ex) {
            if (ex != null) {
                if (selectionKey != null) {
                    selectionKey.cancel();
                }
                Util.close(javaChannel);
            }
        }

    }

    static final class TimeoutTask extends DelayedQueue.DelayTask {

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

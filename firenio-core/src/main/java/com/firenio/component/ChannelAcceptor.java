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
import java.net.BindException;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

import com.firenio.buffer.ByteBuf;
import com.firenio.common.Util;
import com.firenio.component.NioEventLoop.EpollEventLoop;
import com.firenio.component.NioEventLoop.JavaEventLoop;
import com.firenio.concurrent.Waiter;
import com.firenio.log.Logger;
import com.firenio.log.LoggerFactory;

/**
 * @author wangkai
 */
public final class ChannelAcceptor extends ChannelContext {

    private NioEventLoopGroup bindGroup;
    private Logger            logger = LoggerFactory.getLogger(getClass());
    private AcceptorUnsafe    unsafe;

    public ChannelAcceptor(int port) {
        this("0.0.0.0", port);
    }

    public ChannelAcceptor(NioEventLoopGroup group) {
        this(group, "0.0.0.0", 0);
    }

    public ChannelAcceptor(NioEventLoopGroup group, int port) {
        this(group, "0.0.0.0", port);
    }

    public ChannelAcceptor(NioEventLoopGroup group, String host, int port) {
        super(group, host, port);
        if (Native.EPOLL_AVAILABLE) {
            unsafe = new EpollAcceptorUnsafe();
        } else {
            unsafe = new JavaAcceptorUnsafe();
        }
    }

    public ChannelAcceptor(String host, int port) {
        this(new NioEventLoopGroup(), host, port);
    }

    public void bind() throws Exception {
        bind(50);
    }

    public synchronized void bind(final int backlog) throws Exception {
        if (isActive()) {
            return;
        }
        String name = "bind-" + getHost() + ":" + getPort();
        this.bindGroup = new NioEventLoopGroup(name, true);
        this.bindGroup.setEnableMemoryPool(false);
        this.bindGroup.setEnableMemoryPoolDirect(false);
        this.bindGroup.setChannelReadBuffer(0);
        this.bindGroup.setWriteBuffers(0);
        this.getProcessorGroup().setContext(this);
        Util.start(bindGroup);
        Util.start(this);
        final NioEventLoop   bindEventLoop = bindGroup.getNext();
        final Waiter<Object> bindWaiter    = new Waiter<>(this);
        boolean submitted = bindEventLoop.submit(new Runnable() {

            @Override
            public void run() {
                try {
                    unsafe.bind(bindEventLoop, ChannelAcceptor.this, backlog);
                    bindWaiter.call(null, null);
                } catch (Throwable e) {
                    Throwable ex = e;
                    if ("Already bound".equalsIgnoreCase(e.getMessage()) || e instanceof BindException) {
                        ex = new BindException("Already bound at " + getPort());
                    }
                    bindWaiter.call(null, ex);
                    if (bindWaiter.isTimeouted()) {
                        Util.unbind(ChannelAcceptor.this);
                    }
                }
            }
        });
        if (!submitted) {
            Util.unbind(this);
            throw new IOException("failed to bind @ " + getPort());
        }
        if (bindWaiter.await(6000)) {
            Util.unbind(this);
            throw new IOException("time out to bind @ " + getPort());
        }
        if (bindWaiter.isFailed()) {
            Util.unbind(this);
            Throwable ex = bindWaiter.getThrowable();
            if (ex instanceof IOException) {
                throw (IOException) bindWaiter.getThrowable();
            }
            throw new IOException("bind failed", ex);
        }
        logger.info("server listening @" + getServerAddress());
    }

    public void broadcast(ByteBuf buf) {
        getChannelManager().broadcast(buf);
    }

    public void broadcast(Frame frame) throws Exception {
        getChannelManager().broadcast(frame);
    }

    protected AcceptorUnsafe getUnsafe() {
        return unsafe;
    }

    @Override
    public boolean isActive() {
        return unsafe.isActive();
    }

    public synchronized void unbind() {
        Util.close(unsafe);
        Util.stop(bindGroup);
        Util.stop(this);
    }

    static abstract class AcceptorUnsafe implements Closeable {

        abstract void bind(NioEventLoop el, ChannelAcceptor a, int backlog) throws IOException;

        abstract boolean isActive();

    }

    static final class EpollAcceptorUnsafe extends AcceptorUnsafe {

        volatile boolean active;
        NioEventLoop eventLoop;
        int          listen_fd = -1;

        @Override
        void bind(NioEventLoop eventLoop, ChannelAcceptor acceptor, int backlog) throws IOException {
            eventLoop.assertInEventLoop("registerSelector must in event loop");
            this.close();
            this.active = true;
            this.listen_fd = Native.bind(acceptor.getHost(), acceptor.getPort(), backlog);
            Native.throwException(listen_fd);
            EpollEventLoop el = (EpollEventLoop) eventLoop;
            el.ctxs.put(listen_fd, acceptor);
            Native.throwException(Native.epoll_add(el.epfd, listen_fd, Native.EPOLL_IN));
        }

        @Override
        public void close() {
            this.active = false;
            int listen_fd = this.listen_fd;
            if (listen_fd != -1) {
                NioEventLoop eventLoop = this.eventLoop;
                if (eventLoop != null) {
                    EpollEventLoop el = (EpollEventLoop) eventLoop;
                    Native.epoll_del(el.epfd, listen_fd);
                    el.ctxs.remove(listen_fd);
                }
                Native.close(listen_fd);
                this.listen_fd = -1;
            }
        }

        @Override
        boolean isActive() {
            return active;
        }

    }

    static final class JavaAcceptorUnsafe extends AcceptorUnsafe {

        private ServerSocketChannel selectableChannel;
        private ServerSocket        serverSocket;

        @Override
        void bind(NioEventLoop eventLoop, ChannelAcceptor ctx, int backlog) throws IOException {
            eventLoop.assertInEventLoop("registerSelector must in event loop");
            JavaEventLoop el       = (JavaEventLoop) eventLoop;
            Selector      selector = el.getSelector();
            this.close();
            this.selectableChannel = ServerSocketChannel.open();
            this.selectableChannel.configureBlocking(false);
            this.serverSocket = selectableChannel.socket();
            this.selectableChannel.register(selector, SelectionKey.OP_ACCEPT, ctx);
            this.serverSocket.bind(ctx.getServerAddress(), backlog);
        }

        @Override
        public void close() {
            Util.close(serverSocket);
            Util.close(selectableChannel);
        }

        public ServerSocketChannel getSelectableChannel() {
            return selectableChannel;
        }

        @Override
        boolean isActive() {
            ServerSocketChannel channel = this.selectableChannel;
            return channel != null && channel.isOpen();
        }

    }

}

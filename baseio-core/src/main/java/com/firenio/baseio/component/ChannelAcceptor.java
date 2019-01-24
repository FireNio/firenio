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
import java.net.BindException;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

import com.firenio.baseio.TimeoutException;
import com.firenio.baseio.buffer.ByteBuf;
import com.firenio.baseio.common.Util;
import com.firenio.baseio.component.NioEventLoop.EpollNioEventLoopUnsafe;
import com.firenio.baseio.component.NioEventLoop.JavaNioEventLoopUnsafe;
import com.firenio.baseio.concurrent.Waiter;
import com.firenio.baseio.log.Logger;
import com.firenio.baseio.log.LoggerFactory;

/**
 * @author wangkai
 *
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
        if (Native.EPOLL_AVAIABLE) {
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
        this.bindGroup = new NioEventLoopGroup(name);
        this.bindGroup.setEnableMemoryPool(false);
        this.bindGroup.setEnableMemoryPoolDirect(false);
        this.bindGroup.setChannelReadBuffer(0);
        this.bindGroup.setWriteBuffers(0);
        this.bindGroup.setAcceptor(true);
        this.getProcessorGroup().setContext(this);
        Util.start(bindGroup);
        Util.start(this);
        final NioEventLoop bindEventLoop = bindGroup.getNext();
        final Waiter<Object> bindWaiter = new Waiter<>();
        boolean submitted = bindEventLoop.submit(new Runnable() {

            @Override
            public void run() {
                try {
                    unsafe.bind(bindEventLoop, ChannelAcceptor.this, backlog);
                    bindWaiter.call(null, null);
                } catch (Throwable e) {
                    Throwable ex = e;
                    if ("Already bound".equalsIgnoreCase(e.getMessage())
                            || e instanceof BindException) {
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

    public synchronized void unbind() throws TimeoutException {
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
        NioEventLoop     eventLoop;
        int              listenfd = -1;

        @Override
        void bind(NioEventLoop eventLoop, ChannelAcceptor acceptor, int backlog)
                throws IOException {
            eventLoop.assertInEventLoop("registSelector must in event loop");
            this.close();
            this.active = true;
            this.listenfd = Native.bind(acceptor.getHost(), acceptor.getPort(), backlog);
            Native.throwException(listenfd);
            EpollNioEventLoopUnsafe elUnsafe = (EpollNioEventLoopUnsafe) eventLoop.getUnsafe();
            elUnsafe.ctxs.put(listenfd, acceptor);
            Native.throwException(Native.epoll_add(elUnsafe.epfd, listenfd, Native.EPOLLIN));
        }

        @Override
        public void close() throws IOException {
            this.active = false;
            int listenfd = this.listenfd;
            if (listenfd != -1) {
                NioEventLoop el = this.eventLoop;
                if (el != null) {
                    EpollNioEventLoopUnsafe elUnsafe = (EpollNioEventLoopUnsafe) el.getUnsafe();
                    Native.epoll_del(elUnsafe.epfd, listenfd);
                    elUnsafe.ctxs.remove(listenfd);
                }
                Native.close(listenfd);
                this.listenfd = -1;
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
            eventLoop.assertInEventLoop("registSelector must in event loop");
            JavaNioEventLoopUnsafe elUnsafe = (JavaNioEventLoopUnsafe) eventLoop.getUnsafe();
            Selector selector = elUnsafe.getSelector();
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

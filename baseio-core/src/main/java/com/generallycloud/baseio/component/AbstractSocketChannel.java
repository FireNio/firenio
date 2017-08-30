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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;

import com.generallycloud.baseio.ClosedChannelException;
import com.generallycloud.baseio.buffer.ByteBufAllocator;
import com.generallycloud.baseio.buffer.EmptyByteBuf;
import com.generallycloud.baseio.common.ReleaseUtil;
import com.generallycloud.baseio.component.ssl.SslHandler;
import com.generallycloud.baseio.concurrent.ExecutorEventLoop;
import com.generallycloud.baseio.concurrent.LinkedQueue;
import com.generallycloud.baseio.concurrent.ScspLinkedQueue;
import com.generallycloud.baseio.connector.AbstractSocketChannelConnector;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.baseio.protocol.ChannelFuture;
import com.generallycloud.baseio.protocol.DefaultChannelFuture;
import com.generallycloud.baseio.protocol.Future;
import com.generallycloud.baseio.protocol.ProtocolDecoder;
import com.generallycloud.baseio.protocol.ProtocolEncoder;
import com.generallycloud.baseio.protocol.ProtocolFactory;
import com.generallycloud.baseio.protocol.SslFuture;

public abstract class AbstractSocketChannel extends AbstractChannel implements SocketChannel {

    protected ProtocolDecoder            protocolDecoder;
    protected ProtocolEncoder            protocolEncoder;
    protected ProtocolFactory            protocolFactory;
    protected AtomicInteger              writeFutureLength;
    protected ExecutorEventLoop          executorEventLoop;
    protected SSLEngine                  sslEngine;
    protected SslHandler                 sslHandler;
    protected UnsafeSocketSession        session;
    protected transient ChannelFuture    writeFuture;
    protected transient ChannelFuture    readFuture;
    protected transient SslFuture        sslReadFuture;
    protected LinkedQueue<ChannelFuture> writeFutures;
    protected boolean                    opened = true;
    protected SocketChannelThreadContext threadContext;

    private static final Logger          logger = LoggerFactory
            .getLogger(AbstractSocketChannel.class);

    // FIXME 改进network wake 机制
    // FIXME network weak check
    public AbstractSocketChannel(SocketChannelThreadContext context, int channelId) {
        super(context.getByteBufAllocator(), context.getChannelContext(), channelId);
        SocketChannelContext socketChannelContext = context.getChannelContext();
        DefaultChannelFuture f = new DefaultChannelFuture(context.getChannelContext(),
                EmptyByteBuf.getInstance());
        this.protocolFactory = socketChannelContext.getProtocolFactory();
        this.protocolDecoder = socketChannelContext.getProtocolDecoder();
        this.protocolEncoder = socketChannelContext.getProtocolEncoder();
        this.executorEventLoop = context.getExecutorEventLoop();
        this.session = context.getChannelContext().getSessionFactory().newUnsafeSession(this);
        this.writeFutures = new ScspLinkedQueue<>(f);
        this.writeFutureLength = new AtomicInteger();
        this.threadContext = context;
    }

    @Override
    public int getWriteFutureLength() {
        return writeFutureLength.get();
    }

    @Override
    public InetSocketAddress getLocalSocketAddress() {
        if (local == null) {
            try {
                local = getLocalSocketAddress0();
            } catch (IOException e) {
                local = ERROR_SOCKET_ADDRESS;
            }
        }
        return local;
    }

    @Override
    public void finishHandshake(Exception e) {
        if (getContext().getSslContext().isClient()) {
            AbstractSocketChannelConnector connector = (AbstractSocketChannelConnector) getContext()
                    .getChannelService();
            connector.finishConnect(getSession(), e);
        }
    }

    @Override
    protected String getMarkPrefix() {
        return "Tcp";
    }

    @Override
    public ProtocolDecoder getProtocolDecoder() {
        return protocolDecoder;
    }

    @Override
    public ProtocolEncoder getProtocolEncoder() {
        return protocolEncoder;
    }

    @Override
    public ProtocolFactory getProtocolFactory() {
        return protocolFactory;
    }

    @Override
    public ChannelFuture getReadFuture() {
        return readFuture;
    }

    @Override
    public InetSocketAddress getRemoteSocketAddress() {
        if (remote == null) {
            try {
                remote = getRemoteSocketAddress0();
            } catch (IOException e) {
                remote = ERROR_SOCKET_ADDRESS;
            }
        }
        return remote;
    }

    protected abstract InetSocketAddress getRemoteSocketAddress0() throws IOException;

    protected abstract InetSocketAddress getLocalSocketAddress0() throws IOException;

    @Override
    public UnsafeSocketSession getSession() {
        return session;
    }

    @Override
    public int getWriteFutureSize() {
        return writeFutures.size();
    }

    // FIXME 是否使用channel.isOpen()
    @Override
    public boolean isOpened() {
        return opened;
    }

    @Override
    public boolean isEnableSSL() {
        return getContext().isEnableSSL();
    }

    @Override
    public SSLEngine getSSLEngine() {
        return sslEngine;
    }

    @Override
    public void flush(ChannelFuture future) {
        if (future == null || future.flushed()) {
            return;
        }
        if (!isOpened()) {
            future.flush();
            exceptionCaught(getContext().getIoEventHandleAdaptor(), future, 
                    new ClosedChannelException(toString()));
            return;
        }
        try {
            ProtocolEncoder encoder = getProtocolEncoder();
            ByteBufAllocator allocator = getByteBufAllocator();
            // 请勿将future.flush()移到getProtocolEncoder()之前，
            // 有些情况下如协议切换的时候可能需要将此future使用
            // 切换前的协议flush
            encoder.encode(allocator, future.flush());
            doFlush(future);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            exceptionCaught(getContext().getIoEventHandleAdaptor(), future, e);
        }
    }

    private void exceptionCaught(IoEventHandle handle, Future future, Exception ex) {
        try {
            handle.exceptionCaught(getSession(), future, ex);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void doFlush(ChannelFuture f) {
        UnsafeSocketSession session = getSession();
        //这里需要加锁，因为当多个线程同时flush时，判断(writeFutures.size() > 1)
        //会产生误差，导致无法或者延迟触发doFlush操作
        ReentrantLock lock = getCloseLock();
        lock.lock();
        try {
            // 这里最好使用isClosing()判断更合适，但是使用isOpened()判断也没问题
            // 因为doFlush与Close互斥
            if (!isOpened()) {
                f.onException(session, new ClosedChannelException(session.toString()));
                return;
            }
            writeFutures.offer(f);
            int length = writeFutureLength(f.getByteBufLimit());
            if (length > 1024 * 1024 * 10) {
                // FIXME 该连接写入过多啦
            }
            // 如果write futures > 1 说明在offer之后至少有一个write future
            // event loop 在判断complete时返回false
            if (writeFutures.size() > 1) {
                return;
            }
            doFlush0(f);
        } catch (Exception e) {
            f.onException(session, e);
        } finally {
            lock.unlock();
        }
    }

    protected int writeFutureLength(int len) {
        return writeFutureLength.addAndGet(len);
    }

    protected abstract void doFlush0(ChannelFuture future);

    protected void releaseFutures() {

        ReleaseUtil.release(readFuture);
        ReleaseUtil.release(sslReadFuture);

        ClosedChannelException e = null;

        if (writeFuture != null && !writeFuture.isReleased()) {

            e = new ClosedChannelException(session.toString());

            writeFuture.onException(session, e);
        }

        LinkedQueue<ChannelFuture> writeFutures = this.writeFutures;

        if (writeFutures.size() == 0) {
            return;
        }

        ChannelFuture f = writeFutures.poll();

        UnsafeSocketSession session = this.session;

        if (e == null) {
            e = new ClosedChannelException(session.toString());
        }

        for (; f != null;) {

            f.onException(session, e);

            ReleaseUtil.release(f);

            f = writeFutures.poll();
        }
    }

    protected void closeSSL() {

        if (isEnableSSL()) {

            sslEngine.closeOutbound();

            if (getContext().getSslContext().isClient()) {
                doFlush(new DefaultChannelFuture(getContext(), EmptyByteBuf.getInstance()));
            }

            try {
                sslEngine.closeInbound();
            } catch (SSLException e) {}
        }
    }

    @Override
    public SslHandler getSslHandler() {
        return sslHandler;
    }

    @Override
    public void setProtocolDecoder(ProtocolDecoder protocolDecoder) {
        this.protocolDecoder = protocolDecoder;
    }

    @Override
    public void setProtocolEncoder(ProtocolEncoder protocolEncoder) {
        this.protocolEncoder = protocolEncoder;
    }

    @Override
    public void setProtocolFactory(ProtocolFactory protocolFactory) {
        this.protocolFactory = protocolFactory;
    }

    @Override
    public void setReadFuture(ChannelFuture readFuture) {
        this.readFuture = readFuture;
    }

    @Override
    public SslFuture getSslReadFuture() {
        return sslReadFuture;
    }

    @Override
    public void setSslReadFuture(SslFuture future) {
        this.sslReadFuture = future;
    }

    @Override
    public ExecutorEventLoop getExecutorEventLoop() {
        return executorEventLoop;
    }

    protected abstract SocketChannelThreadContext getSocketChannelThreadContext();

    @Override
    public void fireOpend() {
        SocketChannelContext context = getContext();
        if (context.isEnableSSL()) {
            this.sslHandler = getSocketChannelThreadContext().getSslHandler();
            this.sslEngine = context.getSslContext().newEngine();
        }
        if (isEnableSSL() && context.getSslContext().isClient()) {
            doFlush(new DefaultChannelFuture(getContext(), EmptyByteBuf.getInstance()));
        }
        UnsafeSocketSession session = getSession();
        if (!session.isClosed()) {
            threadContext.getSocketSessionManager().putSession(session);
            SocketSessionEventListenerWrapper linkable = context.getSessionEventListenerLink();
            if (linkable != null) {
                linkable.sessionOpened(session);
            }
        }
    }

    protected void fireClosed() {

        SocketSessionEventListenerWrapper linkable = getContext().getSessionEventListenerLink();

        UnsafeSocketSession session = getSession();

        threadContext.getSocketSessionManager().removeSession(session);

        if (linkable != null) {
            linkable.sessionClosed(session);
        }
    }

    @Override
    public boolean inSelectorLoop() {
        return getSocketChannelThreadContext().inEventLoop();
    }

}

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

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.locks.ReentrantLock;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;

import com.generallycloud.baseio.ClosedChannelException;
import com.generallycloud.baseio.buffer.ByteBufAllocator;
import com.generallycloud.baseio.buffer.EmptyByteBuf;
import com.generallycloud.baseio.common.ReleaseUtil;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.ssl.SslHandler;
import com.generallycloud.baseio.concurrent.ExecutorEventLoop;
import com.generallycloud.baseio.concurrent.LinkedQueue;
import com.generallycloud.baseio.concurrent.ScspLinkedQueue;
import com.generallycloud.baseio.connector.AbstractChannelConnector;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.baseio.protocol.ChannelFuture;
import com.generallycloud.baseio.protocol.DefaultChannelFuture;
import com.generallycloud.baseio.protocol.Future;
import com.generallycloud.baseio.protocol.ProtocolCodec;
import com.generallycloud.baseio.protocol.SslFuture;

public abstract class AbstractSocketChannel implements SocketChannel {

    private static final Logger              logger               = LoggerFactory.getLogger(AbstractSocketChannel.class);
    protected static final InetSocketAddress ERROR_SOCKET_ADDRESS = new InetSocketAddress(0);
    protected ByteBufAllocator               byteBufAllocator;
    protected String                         channelDesc;
    protected int                            channelId;
    protected ReentrantLock                  closeLock            = new ReentrantLock();
    protected long                           creationTime         = System.currentTimeMillis();
    protected long                           lastAccess;
    protected String                         localAddr;
    protected int                            localPort;
    protected boolean                        opened               = true;
    protected ProtocolCodec                  protocolCodec;
    protected transient ChannelFuture        readFuture;
    protected String                         remoteAddr;
    protected String                         remoteAddrPort;
    protected int                            remotePort;
    protected UnsafeSocketSession            session;
    protected SSLEngine                      sslEngine;
    protected transient SslFuture           sslReadFuture;
    protected LinkedQueue<ChannelFuture>     writeFutures;

    AbstractSocketChannel(ChannelThreadContext context, int channelId) {
        SocketChannelContext socketChannelContext = context.getChannelContext();
        DefaultChannelFuture f = new DefaultChannelFuture(EmptyByteBuf.get());
        // 认为在第一次Idle之前，连接都是畅通的
        this.channelId = channelId;
        this.byteBufAllocator = context.getByteBufAllocator();
        this.lastAccess = creationTime + socketChannelContext.getSessionIdleTime();
        this.protocolCodec = socketChannelContext.getProtocolCodec();
        this.session = context.getChannelContext().getSessionFactory().newUnsafeSession(this);
        this.writeFutures = new ScspLinkedQueue<>(f);
    }

    @Override
    public void active() {
        this.lastAccess = System.currentTimeMillis();
    }

    protected void closeSSL() {
        if (isEnableSsl()) {
            sslEngine.closeOutbound();
            if (getContext().getSslContext().isClient()) {
                writeFutures.offer(new DefaultChannelFuture(EmptyByteBuf.get(), true));
            }
            try {
                sslEngine.closeInbound();
            } catch (SSLException e) {}
        }
    }

    protected abstract void doFlush0();

    protected void exceptionCaught(Future future, Exception ex) {
        ReleaseUtil.release(future);
        try {
            getChannelThreadContext().getIoEventHandle().exceptionCaught(getSession(), future, ex);
        } catch (Throwable e) {
            logger.error(ex.getMessage(), ex);
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void finishHandshake(Exception e) {
        if (getContext().getSslContext().isClient()) {
            AbstractChannelConnector connector = (AbstractChannelConnector) getContext()
                    .getChannelService();
            connector.finishConnect(getSession(), e);
        }
    }

    protected void fireClosed() {
        getChannelThreadContext().getSocketSessionManager().removeSession(session);
        UnsafeSocketSession session = getSession();
        for (SocketSessionEventListener l : getContext().getSessionEventListeners()) {
            try {
                l.sessionClosed(session);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void fireOpend() {
        //FIXME ..如果这时候连接关闭了如何处理
        //请勿使用remote.getRemoteHost(),可能出现阻塞
        InetSocketAddress remote = getRemoteSocketAddress0();
        InetSocketAddress local = getLocalSocketAddress0();
        remoteAddr = remote.getAddress().getHostAddress();
        remotePort = remote.getPort();
        remoteAddrPort = remoteAddr + ":" + remotePort;
        localAddr = local.getAddress().getHostAddress();
        localPort = local.getPort();
        SocketChannelContext context = getContext();
        if (context.isEnableSsl()) {
            this.sslEngine = context.getSslContext().newEngine(remoteAddr, remotePort);
        }
        if (isEnableSsl() && context.getSslContext().isClient()) {
            flushChannelFuture(new DefaultChannelFuture(EmptyByteBuf.get(), true));
        }
        UnsafeSocketSession session = getSession();
        if (!session.isClosed()) {
            getChannelThreadContext().getSocketSessionManager().putSession(session);
            for (SocketSessionEventListener l : getContext().getSessionEventListeners()) {
                try {
                    l.sessionOpened(session);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public void flush(ChannelFuture future) {
        if (future == null || future.flushed()) {
            return;
        }
        future.flush();
        if (!isOpened()) {
            exceptionCaught(future, new ClosedChannelException(toString()));
            return;
        }
        try {
            future.setNeedSsl(getContext().isEnableSsl());
            ProtocolCodec codec = getProtocolCodec();
            codec.encode(this, future);
            flushChannelFuture(future);
        } catch (Exception e) {
            exceptionCaught(future, e);
        }
    }

    @Override
    public void flushChannelFuture(ChannelFuture future) {
        UnsafeSocketSession session = getSession();
        //这里需要加锁，因为当多个线程同时flush时，判断(writeFutures.size() > 1)
        //会产生误差，导致无法或者延迟触发doFlush操作
        ReentrantLock lock = getCloseLock();
        lock.lock();
        try {
            // 这里最好使用isClosing()判断更合适，但是使用isOpened()判断也没问题
            // 因为doFlush与Close互斥
            if (!isOpened()) {
                exceptionCaught(future, new ClosedChannelException(session.toString()));
                return;
            }
            // FIXME 该连接写入过多啦
            writeFutures.offer(future);
            // 如果write futures > 1 说明在offer之后至少有一个write future
            // event loop 在判断complete时返回false
            if (writeFutures.size() > 1) {
                return;
            }
            doFlush0();
        } catch (Exception e) {
            exceptionCaught(future, e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public ByteBufAllocator getByteBufAllocator() {
        return byteBufAllocator;
    }

    @Override
    public int getChannelId() {
        return channelId;
    }

    protected ReentrantLock getCloseLock() {
        return closeLock;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public Charset getEncoding() {
        return getContext().getEncoding();
    }

    @Override
    public ExecutorEventLoop getExecutorEventLoop() {
        return getChannelThreadContext().getExecutorEventLoop();
    }

    @Override
    public long getLastAccessTime() {
        return lastAccess;
    }

    @Override
    public String getLocalAddr() {
        return localAddr;
    }

    @Override
    public int getLocalPort() {
        return localPort;
    }

    protected abstract InetSocketAddress getLocalSocketAddress0();

    @Override
    public ProtocolCodec getProtocolCodec() {
        return protocolCodec;
    }

    @Override
    public ChannelFuture getReadFuture() {
        return readFuture;
    }

    @Override
    public String getRemoteAddr() {
        return remoteAddr;
    }

    @Override
    public String getRemoteAddrPort() {
        return remoteAddrPort;
    }

    @Override
    public int getRemotePort() {
        return remotePort;
    }

    protected abstract InetSocketAddress getRemoteSocketAddress0();

    @Override
    public UnsafeSocketSession getSession() {
        return session;
    }

    @Override
    public SSLEngine getSSLEngine() {
        return sslEngine;
    }

    @Override
    public SslHandler getSslHandler() {
        return getChannelThreadContext().getSslHandler();
    }

    @Override
    public SslFuture getSslReadFuture() {
        return sslReadFuture;
    }

    @Override
    public int getWriteFutureSize() {
        return writeFutures.size();
    }

    @Override
    public boolean inSelectorLoop() {
        return getChannelThreadContext().inEventLoop();
    }

    @Override
    public boolean isEnableSsl() {
        return getChannelThreadContext().isEnableSsl();
    }

    // FIXME 是否使用channel.isOpen()
    @Override
    public boolean isOpened() {
        return opened;
    }

    protected abstract void close0();

    protected void releaseFutures(ClosedChannelException e) {
        ReleaseUtil.release(readFuture);
        ReleaseUtil.release(sslReadFuture);
        LinkedQueue<ChannelFuture> writeFutures = this.writeFutures;
        if (writeFutures.size() == 0) {
            return;
        }
        ChannelFuture future = writeFutures.poll();
        UnsafeSocketSession session = this.session;
        if (e == null) {
            e = new ClosedChannelException(session.toString());
        }
        for (; future != null;) {
            exceptionCaught(future, e);
            ReleaseUtil.release(future);
            future = writeFutures.poll();
        }
    }

    @Override
    public void setProtocolCodec(ProtocolCodec protocolCodec) {
        this.protocolCodec = protocolCodec;
    }

    @Override
    public void setReadFuture(ChannelFuture readFuture) {
        this.readFuture = readFuture;
    }

    @Override
    public void setSslReadFuture(SslFuture future) {
        this.sslReadFuture = future;
    }

    @Override
    public String toString() {
        if (channelDesc == null) {
            String idStr = Long.toHexString(channelId);
            idStr = "0x" + StringUtil.getZeroString(8 - idStr.length()) + idStr;
            channelDesc = new StringBuilder("[Id(").append(idStr).append(")R/")
                    .append(getRemoteAddr()).append(":").append(getRemotePort()).append("; L:")
                    .append(getLocalPort()).append("]").toString();
        }
        return channelDesc;
    }

}

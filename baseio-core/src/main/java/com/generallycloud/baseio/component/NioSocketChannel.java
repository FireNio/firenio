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
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.ByteBufAllocator;
import com.generallycloud.baseio.buffer.EmptyByteBuf;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.ReleaseUtil;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.common.ThrowableUtil;
import com.generallycloud.baseio.component.ssl.SslHandler;
import com.generallycloud.baseio.concurrent.ExecutorEventLoop;
import com.generallycloud.baseio.concurrent.LinkedQueue;
import com.generallycloud.baseio.concurrent.ScspLinkedQueue;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.baseio.protocol.ChannelFuture;
import com.generallycloud.baseio.protocol.DefaultChannelFuture;
import com.generallycloud.baseio.protocol.Future;
import com.generallycloud.baseio.protocol.ProtocolCodec;
import com.generallycloud.baseio.protocol.SslFuture;

public class NioSocketChannel implements NioEventLoopTask {
    
    
    private static final ClosedChannelException CLOSED_WHEN_FLUSH = ThrowableUtil.unknownStackTrace(
            new ClosedChannelException(), NioSocketChannel.class, "flush(...)");
    private static final ClosedChannelException CLOSED_CHANNEL = ThrowableUtil.unknownStackTrace(
            new ClosedChannelException(), NioSocketChannel.class, "channel closed");
    private static final InetSocketAddress ERROR_SOCKET_ADDRESS = new InetSocketAddress(0);
    private static final Logger            logger               = LoggerFactory
            .getLogger(NioSocketChannel.class);
    private ByteBufAllocator               allocator;
    private SocketChannel                  channel;
    private String                         channelDesc;
    private Integer                        channelId;
    private ReentrantLock                  closeLock            = new ReentrantLock();
    private ChannelContext                 context;
    private long                           creationTime         = System.currentTimeMillis();
    private ChannelFuture[]                currentWriteFutures;
    private int                            currentWriteFuturesLen;
    private final boolean                  enableSsl;
    private final NioEventLoop             eventLoop;
    private long                           lastAccess;
    private String                         localAddr;
    private int                            localPort;
    private boolean                        opened               = true;
    private ProtocolCodec                  protocolCodec;
    private transient ChannelFuture        readFuture;
    private ByteBuf                        remainingBuf;
    private String                         remoteAddr;
    private String                         remoteAddrPort;
    private int                            remotePort;
    private final SelectionKey             selectionKey;
    private SocketSession                  session;
    private SSLEngine                      sslEngine;
    private transient SslFuture            sslReadFuture;
    private LinkedQueue<ChannelFuture>     writeFutures;
    private ExecutorEventLoop              executorEventLoop;

    NioSocketChannel(NioEventLoop eventLoop, SelectionKey selectionKey, ChannelContext context,
            int channelId) {
        NioEventLoopGroup group = eventLoop.getGroup();
        this.eventLoop = eventLoop;
        this.context = context;
        this.channelId = channelId;
        this.selectionKey = selectionKey;
        this.enableSsl = context.isEnableSsl();
        this.allocator = eventLoop.allocator();
        this.protocolCodec = context.getProtocolCodec();
        this.currentWriteFutures = new ChannelFuture[group.getWriteBuffers()];
        this.executorEventLoop = context.getExecutorEventLoopGroup().getNext();
        this.channel = (SocketChannel) selectionKey.channel();
        this.lastAccess = creationTime + group.getIdleTime();
        this.session = context.getSessionFactory().newUnsafeSession(this);
        this.writeFutures = new ScspLinkedQueue<>(new DefaultChannelFuture(EmptyByteBuf.get()));
    }

    NioSocketChannel(ChannelContext context, ByteBufAllocator allocator) {
        this.enableSsl = false;
        this.context = context;
        this.allocator = allocator;
        this.eventLoop = null;
        this.selectionKey = null;
    }

    private void accept(ByteBuf buffer) throws Exception {
        final ProtocolCodec codec = getProtocolCodec();
        final ForeFutureAcceptor acceptor = getContext().getForeFutureAcceptor();
        final NioEventLoop eventLoop = this.eventLoop;
        final ByteBufAllocator allocator = this.allocator;
        final List<ChannelFuture> readFutures = eventLoop.getReadFutures();
        final int maxReadFuture = eventLoop.getGroup().getReadFutures();
        for (;;) {
            if (!buffer.hasRemaining()) {
                break;
            }
            ChannelFuture future = getReadFuture();
            boolean setFutureNull = true;
            if (future == null) {
                future = codec.decode(this, buffer);
                setFutureNull = false;
            }
            try {
                if (!future.read(this, buffer)) {
                    if (!setFutureNull) {
                        setReadFuture(future);
                    }
                    if (buffer.hasRemaining()) {
                        ByteBuf remaining = allocator.allocate(buffer.remaining());
                        remaining.read(buffer);
                        remaining.flip();
                        setRemainingBuf(remaining);
                    }
                    break;
                }
            } catch (Throwable e) {
                future.release(eventLoop);
                acceptor.accept(session, readFutures);
                readFutures.clear();
                if (e instanceof IOException) {
                    throw (IOException) e;
                }
                throw new IOException("exception occurred when do decode," + e.getMessage(), e);
            }
            if (setFutureNull) {
                setReadFuture(null);
            }
            future.release(eventLoop);
            readFutures.add(future);
            if (readFutures.size() == maxReadFuture) {
                acceptor.accept(session, readFutures);
                readFutures.clear();
            }
        }
        acceptor.accept(session, readFutures);
        readFutures.clear();
    }

    public ByteBufAllocator allocator() {
        return allocator;
    }

    @Override
    public void close() throws IOException {
        if (!isOpened()) {
            return;
        }
        if (inEventLoop()) {
            close0();
        } else {
            eventLoop.dispatch(new NioEventLoopTask() {

                @Override
                public void close() {
                    NioSocketChannel.this.close0();
                }

                @Override
                public void fireEvent(NioEventLoop eventLoop) {
                    NioSocketChannel.this.close0();
                }
            });
        }
    }

    private void close0() {
        if (!isOpened()) {
            return;
        }
        ReentrantLock lock = getCloseLock();
        lock.lock();
        try {
            closeSSL();
            try {
                write();
            } catch (Exception e) {}
            releaseFutures();
            CloseUtil.close(channel);
            selectionKey.attach(null);
            selectionKey.cancel();
            fireClosed();
            opened = false;
        } finally {
            lock.unlock();
        }
    }

    private void closeSSL() {
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

    private void exceptionCaught(Future future, Exception ex) {
        ReleaseUtil.release((ChannelFuture) future, eventLoop);
        try {
            getContext().getIoEventHandle().exceptionCaught(getSession(), future, ex);
        } catch (Throwable e) {
            logger.error(ex.getMessage(), ex);
            logger.error(e.getMessage(), e);
        }
    }

    public void finishHandshake(Exception e) {
        if (getContext().getSslContext().isClient()) {
            ChannelConnector connector = (ChannelConnector) getContext().getChannelService();
            connector.finishConnect(getSession(), e);
        }
    }

    private void fireClosed() {
        eventLoop.removeSession(session);
        SocketSession session = getSession();
        for (SessionEventListener l : getContext().getSessionEventListeners()) {
            try {
                l.sessionClosed(session);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void fireEvent(NioEventLoop eventLoop) throws IOException {
        if (!isOpened()) {
            close();
            return;
        }
        write();
    }

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
        ChannelContext context = getContext();
        if (context.isEnableSsl()) {
            this.sslEngine = context.getSslContext().newEngine(remoteAddr, remotePort);
        }
        if (isEnableSsl() && context.getSslContext().isClient()) {
            flushChannelFuture(new DefaultChannelFuture(EmptyByteBuf.get(), true));
        }
        SocketSession session = getSession();
        if (!session.isClosed()) {
            eventLoop.putSession(session);
            for (SessionEventListener l : getContext().getSessionEventListeners()) {
                try {
                    l.sessionOpened(session);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    public void flush(ChannelFuture future) {
        if (future == null || future.flushed()) {
            return;
        }
        future.flush();
        if (!isOpened()) {
            exceptionCaught(future, CLOSED_WHEN_FLUSH);
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
    
    public void unsafeFlush(ChannelFuture future) {
        if (future == null || future.flushed()) {
            return;
        }
        future.flush();
        if (!isOpened()) {
            exceptionCaught(future, CLOSED_WHEN_FLUSH);
            return;
        }
        try {
            future.setNeedSsl(getContext().isEnableSsl());
            ProtocolCodec codec = getProtocolCodec();
            codec.encode(this, future);
            unsafeFlushChannelFuture(future);
        } catch (Exception e) {
            exceptionCaught(future, e);
        }
    }

    public void flushFutures(Collection<ChannelFuture> futures) {
        if (futures == null || futures.isEmpty()) {
            return;
        }
        if (!isOpened()) {
            for (ChannelFuture future : futures) {
                if (future.isHeartbeat()) {
                    continue;
                }
                exceptionCaught(future, CLOSED_WHEN_FLUSH);
            }
            return;
        }
        try {
            for (ChannelFuture f : futures) {
                if (f.isSilent() || f.isHeartbeat()) {
                    continue;
                }
                f.flush();
                f.setNeedSsl(getContext().isEnableSsl());
                ProtocolCodec codec = getProtocolCodec();
                codec.encode(this, f);
            }
        } catch (Exception e) {
            for (ChannelFuture f : futures) {
                if (f.isSilent() || f.isHeartbeat()) {
                    continue;
                }
                exceptionCaught(f, e);
            }
            CloseUtil.close(this);
            return;
        }
        flushChannelFutures(futures);
    }
    
    // 该方法必须在eventloop线程执行，当前无未写入数据时，立即写入，否则追加写入
    public void unsafeFlushChannelFuture(ChannelFuture future) {
        if (!inEventLoop()) {
            throw new RuntimeException("not in eventloop");
        } 
        if (!isOpened()) {
            exceptionCaught(future, CLOSED_WHEN_FLUSH);
            return;
        }
        if (currentWriteFuturesLen == 0 && writeFutures.size() == 0) {
            write(future);
        }else{
            writeFutures.offer(future);
            try {
                write();
            } catch (Throwable t) {
                CloseUtil.close(this);
            }
        }
    }

    public void flushChannelFuture(ChannelFuture future) {
        if (inEventLoop()) {
            if (!isOpened()) {
                exceptionCaught(future, CLOSED_WHEN_FLUSH);
                return;
            }
            writeFutures.offer(future);
            try {
                write();
            } catch (Throwable t) {
                CloseUtil.close(this);
            }
        } else {
            ReentrantLock lock = getCloseLock();
            lock.lock();
            try {
                if (!isOpened()) {
                    exceptionCaught(future, CLOSED_WHEN_FLUSH);
                    return;
                }
                writeFutures.offer(future);
                if (writeFutures.size() != 1) {
                    return;
                }
            } finally {
                lock.unlock();
            }
            eventLoop.dispatch(this);
        }
    }

    public void flushChannelFutures(Collection<ChannelFuture> futures) {
        if (futures == null || futures.isEmpty()) {
            return;
        }
        if (inEventLoop()) {
            try {
                if (!isOpened()) {
                    for (ChannelFuture future : futures) {
                        exceptionCaught(future, CLOSED_WHEN_FLUSH);
                    }
                    return;
                }
                for (ChannelFuture future : futures) {
                    if (future.isSilent() || future.isHeartbeat()) {
                        continue;
                    }
                    writeFutures.offer(future);
                }
            } catch (Exception e) {
                //will happen ?
                for (ChannelFuture f : futures) {
                    if (f.isSilent() || f.isHeartbeat()) {
                        continue;
                    }
                    exceptionCaught(f, e);
                }
                return;
            }
            try {
                write();
            } catch (Throwable t) {
                CloseUtil.close(this);
            }
        } else {
            ReentrantLock lock = getCloseLock();
            lock.lock();
            try {
                int size = 0;
                if (!isOpened()) {
                    for (ChannelFuture future : futures) {
                        if (future.isSilent() || future.isHeartbeat()) {
                            continue;
                        }
                        exceptionCaught(future, CLOSED_WHEN_FLUSH);
                    }
                    return;
                }
                for (ChannelFuture future : futures) {
                    if (future.isSilent() || future.isHeartbeat()) {
                        continue;
                    }
                    size++;
                    writeFutures.offer(future);
                }
                if (writeFutures.size() != size) {
                    return;
                }
            } catch (Exception e) {
                //will happen ?
                for (ChannelFuture future : futures) {
                    if (future.isSilent() || future.isHeartbeat()) {
                        continue;
                    }
                    exceptionCaught(future, e);
                }
                return;
            } finally {
                lock.unlock();
            }
            eventLoop.dispatch(this);
        }
    }

    public Integer getChannelId() {
        return channelId;
    }

    private ReentrantLock getCloseLock() {
        return closeLock;
    }

    public ChannelContext getContext() {
        return context;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public Charset getEncoding() {
        return getContext().getEncoding();
    }

    public NioEventLoop getEventLoop() {
        return eventLoop;
    }

    public ExecutorEventLoop getExecutorEventLoop() {
        return executorEventLoop;
    }

    public long getLastAccessTime() {
        return lastAccess;
    }

    public String getLocalAddr() {
        return localAddr;
    }

    public int getLocalPort() {
        return localPort;
    }

    private InetSocketAddress getLocalSocketAddress0() {
        try {
            return (InetSocketAddress) channel.getLocalAddress();
        } catch (IOException e) {
            return ERROR_SOCKET_ADDRESS;
        }
    }

    public <T> T getOption(SocketOption<T> name) throws IOException {
        return channel.getOption(name);
    }

    public ProtocolCodec getProtocolCodec() {
        return protocolCodec;
    }

    public ChannelFuture getReadFuture() {
        return readFuture;
    }

    public ByteBuf getRemainingBuf() {
        return remainingBuf;
    }

    public String getRemoteAddr() {
        return remoteAddr;
    }

    public String getRemoteAddrPort() {
        return remoteAddrPort;
    }

    public int getRemotePort() {
        return remotePort;
    }

    private InetSocketAddress getRemoteSocketAddress0() {
        try {
            return (InetSocketAddress) channel.getRemoteAddress();
        } catch (Exception e) {}
        return ERROR_SOCKET_ADDRESS;
    }

    public SocketSession getSession() {
        return session;
    }

    public SSLEngine getSSLEngine() {
        return sslEngine;
    }

    public SslHandler getSslHandler() {
        return eventLoop.getSslHandler();
    }

    public SslFuture getSslReadFuture() {
        return sslReadFuture;
    }

    public int getWriteFutureSize() {
        return writeFutures.size();
    }

    public boolean inEventLoop() {
        return eventLoop.inEventLoop();
    }

    private void interestRead(SelectionKey key) {
        int interestOps = key.interestOps();
        if (SelectionKey.OP_READ != interestOps) {
            key.interestOps(SelectionKey.OP_READ);
        }
    }

    private void interestWrite(SelectionKey key) {
        if ((SelectionKey.OP_READ | SelectionKey.OP_WRITE) != key.interestOps()) {
            key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        }
    }

    public boolean isBlocking() {
        return channel.isBlocking();
    }

    public boolean isEnableSsl() {
        return enableSsl;
    }

    public boolean isOpened() {
        return opened;
    }

    protected void read(ByteBuf buf) throws Exception {
        lastAccess = System.currentTimeMillis();
        buf.clear();
        if (!isEnableSsl()) {
            readRemainingBuf(buf);
        }
        SocketChannel javaChannel = javaChannel();
        int length = javaChannel.read(buf.nioBuffer());
        if (length < 1) {
            if (length == -1) {
                CloseUtil.close(this);
            }
            return;
        }
        buf.reverse();
        buf.flip();
        SslFuture sslTemporary = eventLoop.getSslTemporary();
        if (isEnableSsl()) {
            for (;;) {
                if (!buf.hasRemaining()) {
                    return;
                }
                SslFuture future = getSslReadFuture();
                boolean setFutureNull = true;
                if (future == null) {
                    future = sslTemporary.reset();
                    setFutureNull = false;
                }
                if (!future.read(this, buf)) {
                    if (!setFutureNull) {
                        if (future == sslTemporary) {
                            future = future.copy(this);
                        }
                        this.setSslReadFuture(future);
                    }
                    return;
                }
                if (setFutureNull) {
                    this.setSslReadFuture(null);
                }
                SslHandler sslHandler = getSslHandler();
                ByteBuf product;
                try {
                    product = sslHandler.unwrap(this, future.getByteBuf());
                } finally {
                    ReleaseUtil.release(future, this.eventLoop);
                }
                if (product == null) {
                    continue;
                }
                accept(product);
            }
        } else {
            accept(buf);
        }
    }

    private void releaseFutures() {
        if (currentWriteFuturesLen > 0) {
            for (int i = 0; i < currentWriteFuturesLen; i++) {
                exceptionCaught(currentWriteFutures[i], CLOSED_CHANNEL);
            }
        }
        NioEventLoop eventLoop = this.eventLoop;
        ReleaseUtil.release(readFuture, eventLoop);
        ReleaseUtil.release(sslReadFuture, eventLoop);
        ReleaseUtil.release(remainingBuf);
        LinkedQueue<ChannelFuture> writeFutures = this.writeFutures;
        if (writeFutures.size() == 0) {
            return;
        }
        ChannelFuture future = writeFutures.poll();
        for (; future != null;) {
            exceptionCaught(future, CLOSED_CHANNEL);
            ReleaseUtil.release(future, eventLoop);
            future = writeFutures.poll();
        }
    }

    public <T> void setOption(SocketOption<T> name, T value) throws IOException {
        channel.setOption(name, value);
    }

    public void setProtocolCodec(ProtocolCodec protocolCodec) {
        this.protocolCodec = protocolCodec;
    }

    public void setReadFuture(ChannelFuture readFuture) {
        this.readFuture = readFuture;
    }

    public void setRemainingBuf(ByteBuf remainingBuf) {
        this.remainingBuf = remainingBuf;
    }
    
    public void readRemainingBuf(ByteBuf dst){
        ByteBuf remainingBuf = this.remainingBuf;
        if (remainingBuf == null) {
            return;
        }
        dst.read(remainingBuf);
        remainingBuf.release(remainingBuf.getReleaseVersion());
        this.remainingBuf = null;
    }

    public void setSslReadFuture(SslFuture future) {
        this.sslReadFuture = future;
    }

    // FIXME 这里有问题

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

    private void write(ChannelFuture future) {
        try {
            if (future.isNeedSsl()) {
                future.setNeedSsl(false);
                // FIXME 部分情况下可以不在业务线程做wrapssl
                ByteBuf old = future.getByteBuf();
                long version = old.getReleaseVersion();
                SslHandler handler = eventLoop.getSslHandler();
                try {
                    ByteBuf newBuf = handler.wrap(this, old);
                    newBuf.nioBuffer();
                    future.setByteBuf(newBuf);
                } finally {
                    ReleaseUtil.release(old, version);
                }
            }
            ByteBuf buf = future.getByteBuf();
            channel.write(buf.nioBuffer());
            buf.reverse();
            if (buf.hasRemaining()) {
                currentWriteFuturesLen = 1;
                currentWriteFutures[0] = future;
                interestWrite(selectionKey);
                return;
            } else {
                try {
                    future.release(eventLoop);
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                }
                try {
                    context.getIoEventHandle().futureSent(session, future);
                } catch (Throwable e) {
                    logger.debug(e.getMessage(), e);
                }
                interestRead(selectionKey);
            }
        } catch (Exception e) {
            CloseUtil.close(this);
            exceptionCaught(future, e);
        }
    }

    protected void write() throws IOException {
        final NioEventLoop eventLoop = this.eventLoop;
        final ChannelFuture[] currentWriteFutures = this.currentWriteFutures;
        final LinkedQueue<ChannelFuture> writeFutures = this.writeFutures;
        final ByteBuffer[] writeBuffers = eventLoop.getWriteBuffers();
        final int maxLen = currentWriteFutures.length;
        for (;;) {
            int i = currentWriteFuturesLen;
            for (; i < maxLen; i++) {
                ChannelFuture future = writeFutures.poll();
                if (future == null) {
                    break;
                }
                currentWriteFutures[i] = future;
            }
            currentWriteFuturesLen = i;
            if (currentWriteFuturesLen == 0) {
                interestRead(selectionKey);
                return;
            }
            //FIXME ...是否要清空buffers
            for (i = 0; i < currentWriteFuturesLen; i++) {
                ChannelFuture future = currentWriteFutures[i];
                if (future.isNeedSsl()) {
                    future.setNeedSsl(false);
                    // FIXME 部分情况下可以不在业务线程做wrapssl
                    ByteBuf old = future.getByteBuf();
                    long version = old.getReleaseVersion();
                    SslHandler handler = eventLoop.getSslHandler();
                    try {
                        ByteBuf newBuf = handler.wrap(this, old);
                        newBuf.nioBuffer();
                        future.setByteBuf(newBuf);
                    } finally {
                        ReleaseUtil.release(old, version);
                    }
                }
                writeBuffers[i] = future.getByteBuf().nioBuffer();
            }
            IoEventHandle ioEventHandle = context.getIoEventHandle();
            if (currentWriteFuturesLen == 1) {
                ByteBuffer nioBuf = writeBuffers[0];
                channel.write(nioBuf);
                if (nioBuf.hasRemaining()) {
                    currentWriteFutures[0].getByteBuf().reverse();
                    interestWrite(selectionKey);
                    return;
                } else {
                    ChannelFuture future = currentWriteFutures[0];
                    currentWriteFutures[0] = null;
                    try {
                        future.release(eventLoop);
                    } catch (Throwable e) {
                        logger.error(e.getMessage(), e);
                    }
                    try {
                        ioEventHandle.futureSent(session, future);
                    } catch (Throwable e) {
                        logger.debug(e.getMessage(), e);
                    }
                    currentWriteFuturesLen = 0;
                    interestRead(selectionKey);
                    return;
                }
            } else {
                channel.write(writeBuffers, 0, currentWriteFuturesLen);
                for (i = 0; i < currentWriteFuturesLen; i++) {
                    ChannelFuture future = currentWriteFutures[i];
                    if (writeBuffers[i].hasRemaining()) {
                        int remain = currentWriteFuturesLen - i;
                        if (remain > 16) {
                            System.arraycopy(currentWriteFutures, i, currentWriteFutures, 0,
                                    remain);
                        } else {
                            for (int j = 0; j < remain; j++) {
                                currentWriteFutures[j] = currentWriteFutures[i + j];
                            }
                        }
                        for (int j = currentWriteFuturesLen - i; j < maxLen; j++) {
                            currentWriteFutures[j] = null;
                        }
                        future.getByteBuf().reverse();
                        currentWriteFuturesLen = remain;
                        interestWrite(selectionKey);
                        return;
                    } else {
                        try {
                            future.release(eventLoop);
                        } catch (Throwable e) {
                            logger.error(e.getMessage(), e);
                        }
                        try {
                            ioEventHandle.futureSent(session, future);
                        } catch (Throwable e) {
                            logger.debug(e.getMessage(), e);
                        }
                    }
                }
                for (int j = 0; j < currentWriteFuturesLen; j++) {
                    currentWriteFutures[j] = null;
                }
                if (currentWriteFuturesLen != maxLen) {
                    currentWriteFuturesLen = 0;
                    interestRead(selectionKey);
                    return;
                }
                currentWriteFuturesLen = 0;
            }
        }
    }

    protected SocketChannel javaChannel() {
        return channel;
    }

}

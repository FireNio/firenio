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
import java.net.InetSocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.ByteBufAllocator;
import com.generallycloud.baseio.buffer.EmptyByteBuf;
import com.generallycloud.baseio.collection.Attributes;
import com.generallycloud.baseio.collection.AttributesImpl;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.ReleaseUtil;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.common.ThrowableUtil;
import com.generallycloud.baseio.component.ChannelContext.HeartBeatLogger;
import com.generallycloud.baseio.component.ssl.SslHandler;
import com.generallycloud.baseio.concurrent.ExecutorEventLoop;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.baseio.protocol.Future;
import com.generallycloud.baseio.protocol.ProtocolCodec;
import com.generallycloud.baseio.protocol.SslFuture;

public final class NioSocketChannel extends AttributesImpl
        implements NioEventLoopTask, Attributes, Closeable {

    private static final ClosedChannelException CLOSED_WHEN_FLUSH    = ThrowableUtil
            .unknownStackTrace(new ClosedChannelException(), NioSocketChannel.class, "flush(...)");
    private static final InetSocketAddress      ERROR_SOCKET_ADDRESS = new InetSocketAddress(0);
    private static final Logger                 logger               = LoggerFactory
            .getLogger(NioSocketChannel.class);
    private final ByteBufAllocator              allocator;
    private final SocketChannel                 channel;
    private final String                        channelDesc;
    private final Integer                       channelId;
    private final ReentrantLock                 closeLock            = new ReentrantLock();
    private final ChannelContext                context;
    private final long                          creationTime         = System.currentTimeMillis();
    private final ByteBuf[]                     currentWriteBufs;
    private int                                 currentWriteBufsLen;
    private final boolean                       enableSsl;
    private final NioEventLoop                  eventLoop;
    private final ExecutorEventLoop             executorEventLoop;
    private IoEventHandle                       ioEventHandle;
    private long                                lastAccess;
    private final String                        localAddr;
    private final int                           localPort;
    private final int                           maxWriteBacklog;
    private boolean                             opened               = true;
    private ProtocolCodec                       protocolCodec;
    private transient Future                    readFuture;
    private ByteBuf                             remainingBuf;
    private final String                        remoteAddr;
    private final String                        remoteAddrPort;
    private final int                           remotePort;
    private final SelectionKey                  selectionKey;
    private final SSLEngine                     sslEngine;
    private transient SslFuture                 sslReadFuture;
    private final Queue<ByteBuf>                writeBufs;

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
        this.maxWriteBacklog = context.getMaxWriteBacklog();
        this.currentWriteBufs = new ByteBuf[group.getWriteBuffers()];
        this.executorEventLoop = context.getExecutorEventLoopGroup().getNext();
        this.channel = (SocketChannel) selectionKey.channel();
        this.lastAccess = creationTime + group.getIdleTime();
        this.writeBufs = new ConcurrentLinkedQueue<>();
        //请勿使用remote.getRemoteHost(),可能出现阻塞
        InetSocketAddress remote = getRemoteSocketAddress0();
        InetSocketAddress local = getLocalSocketAddress0();
        String idhex = Long.toHexString(channelId);
        this.remoteAddr = remote.getAddress().getHostAddress();
        this.remotePort = remote.getPort();
        this.remoteAddrPort = remoteAddr + ":" + remotePort;
        this.localAddr = local.getAddress().getHostAddress();
        this.localPort = local.getPort();
        this.channelDesc = new StringBuilder("[Id(0x")
                .append(StringUtil.getZeroString(8 - idhex.length())).append(idhex).append(")R/")
                .append(getRemoteAddr()).append(":").append(getRemotePort()).append("; L:")
                .append(getLocalPort()).append("]").toString();
        if (context.isEnableSsl()) {
            this.sslEngine = context.getSslContext().newEngine(remoteAddr, remotePort);
        } else {
            this.sslEngine = null;
        }
    }

    private void accept(ByteBuf buffer) throws Exception {
        final ProtocolCodec codec = this.protocolCodec;
        final IoEventHandle eventHandle = this.ioEventHandle;
        final ByteBufAllocator allocator = this.allocator;
        final HeartBeatLogger heartBeatLogger = context.getHeartBeatLogger();
        final boolean enableWorkEventLoop = context.isEnableWorkEventLoop();
        Future future = getReadFuture();
        if (future == null) {
            future = codec.decode(this, buffer);
        }
        try {
            for (;;) {
                if (!future.read(this, buffer)) {
                    setReadFuture(future);
                    if (buffer.hasRemaining()) {
                        ByteBuf remaining = allocator.allocate(buffer.remaining());
                        remaining.read(buffer);
                        remaining.flip();
                        setRemainingBuf(remaining);
                    }
                    break;
                }
                if (future.isSilent()) {
                    if (future.isPing()) {
                        heartBeatLogger.logPing(this);
                        Future f = codec.createPONGPacket(this, future);
                        if (f != null) {
                            flush(f);
                        }
                    } else if (future.isPong()) {
                        heartBeatLogger.logPong(this);
                    }
                } else {
                    if (enableWorkEventLoop) {
                        accept(eventHandle, future);
                    } else {
                        try {
                            eventHandle.accept(this, future);
                        } catch (Exception e) {
                            eventHandle.exceptionCaught(this, future, e);
                        }
                    }
                }
                if (!buffer.hasRemaining()) {
                    setReadFuture(null);
                    break;
                }
                future = codec.decode(this, buffer);
            }
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            throw new IOException("exception occurred when do decode," + e.getMessage(), e);
        }
    }

    private void accept(final IoEventHandle eventHandle, final Future future) {
        getExecutorEventLoop().dispatch(new Runnable() {
            @Override
            public void run() {
                try {
                    eventHandle.accept(NioSocketChannel.this, future);
                } catch (Exception e) {
                    eventHandle.exceptionCaught(NioSocketChannel.this, future, e);
                }
            }
        });
    }

    public ByteBufAllocator allocator() {
        return allocator;
    }

    @Override
    public void close() {
        if (isClosed()) {
            return;
        }
        if (inEventLoop()) {
            close0();
        } else {
            eventLoop.dispatch(new NioEventLoopTask() {

                @Override
                public void fireEvent(NioEventLoop eventLoop) {
                    NioSocketChannel.this.close0();
                }
            });
        }
    }

    private void close0() {
        if (isClosed()) {
            return;
        }
        ReentrantLock lock = getCloseLock();
        lock.lock();
        try {
            if (isClosed()) {
                return;
            }
            opened = false;
            closeSSL();
            try {
                write();
            } catch (Exception e) {}
            releaseBufs();
            CloseUtil.close(channel);
            selectionKey.attach(null);
            selectionKey.cancel();
            fireClosed();
        } finally {
            lock.unlock();
        }
    }

    private void closeSSL() {
        if (isEnableSsl()) {
            sslEngine.closeOutbound();
            if (getContext().getSslContext().isClient()) {
                SslHandler handler = FastThreadLocal.get().getSslHandler();
                try {
                    writeBufs.offer(handler.wrap(this, EmptyByteBuf.get()));
                } catch (IOException e) {
                }
            }
            try {
                sslEngine.closeInbound();
            } catch (SSLException e) {}
        }
    }

    public ByteBuf encode(Future future) throws IOException {
        if (future == null) {
            return null;
        }
        if (isClosed()) {
            return null;
        }
        ByteBuf buf = getProtocolCodec().encode(this, future);
        if (enableSsl) {
            FastThreadLocal l = FastThreadLocal.get();
            ByteBuf old = buf;
            SslHandler handler = l.getSslHandler();
            try {
                buf = handler.wrap(this, old);
            } finally {
                old.release(old.getReleaseVersion());
            }
        }
        return buf;
    }

    private void exceptionCaught(Future future, Exception ex) {
        future.release(eventLoop);
        try {
            getIoEventHandle().exceptionCaught(this, future, ex);
        } catch (Throwable e) {
            logger.error(ex.getMessage(), ex);
            logger.error(e.getMessage(), e);
        }
    }

    public void finishHandshake(Exception e) {
        if (getContext().getSslContext().isClient()) {
            ChannelConnector connector = (ChannelConnector) getContext().getChannelService();
            connector.finishConnect(this, e);
        }
    }

    private void fireClosed() {
        NioSocketChannel channel = this;
        eventLoop.removeChannel(channel);
        for (ChannelEventListener l : getContext().getChannelEventListeners()) {
            try {
                l.channelClosed(channel);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void fireEvent(NioEventLoop eventLoop) throws IOException {
        if (isClosed()) {
            return;
        }
        write();
    }

    public void fireOpend() throws IOException {
        //FIXME ..如果这时候连接关闭了如何处理
        if (isEnableSsl() && context.getSslContext().isClient()) {
            SslHandler handler = FastThreadLocal.get().getSslHandler();
            flush(handler.wrap(this, EmptyByteBuf.get()));
        }
        eventLoop.putChannel(this);
        for (ChannelEventListener l : getContext().getChannelEventListeners()) {
            try {
                l.channelOpened(this);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        if (ioEventHandle == null) {
            ioEventHandle = context.getIoEventHandle();
        }
    }

    /**
     * flush已encode的future
     * @param buf
     */
    public void flush(ByteBuf buf) {
        final Queue<ByteBuf> writeBufs = this.writeBufs;
        if (inEventLoop()) {
            if (isClosed()) {
                return;
            }
            if (currentWriteBufsLen == 0 && writeBufs.size() == 0) {
                write(buf);
            } else {
                writeBufs.offer(buf);
                if (maxWriteBacklog != Integer.MAX_VALUE) {
                    if (writeBufs.size() > maxWriteBacklog) {
                        close();
                        return;
                    }
                }
                try {
                    write();
                } catch (Throwable t) {
                    close();
                }
            }
        } else {
            ReentrantLock lock = getCloseLock();
            lock.lock();
            try {
                if (isClosed()) {
                    return;
                }
                writeBufs.offer(buf);
                if (maxWriteBacklog != Integer.MAX_VALUE) {
                    if (writeBufs.size() > maxWriteBacklog) {
                        close();
                        return;
                    }
                }
                if (writeBufs.size() != 1) {
                    return;
                }
            } finally {
                lock.unlock();
            }
            eventLoop.dispatch(this);
        }
    }

    /**
     * flush未encode的future
     * @param f
     */
    public void flush(Future future) {
        if (future == null || future.flushed()) {
            return;
        }
        if (isClosed()) {
            exceptionCaught(future, CLOSED_WHEN_FLUSH);
            return;
        }
        ByteBuf buf = null;
        try {
            buf = getProtocolCodec().encode(this, future);
            future.flush();
            future.release(eventLoop);
            if (enableSsl) {
                FastThreadLocal l = FastThreadLocal.get();
                ByteBuf old = buf;
                SslHandler handler = l.getSslHandler();
                try {
                    buf = handler.wrap(this, old);
                } finally {
                    old.release(old.getReleaseVersion());
                }
            }
        } catch (Exception e) {
            ReleaseUtil.release(buf);
            exceptionCaught(future, e);
            return;
        }
        flush(buf);
    }

    //FIXME ..使用该方法貌似会性能下降？查找原因
    public void flush(List<ByteBuf> bufs) {
        if (bufs == null || bufs.isEmpty()) {
            return;
        }
        if (inEventLoop()) {
            if (isClosed()) {
                ReleaseUtil.release(bufs);
                return;
            }
            final int bufsSize = bufs.size();
            final Queue<ByteBuf> writeBufs = this.writeBufs;
            if (writeBufs.size() == 0) {
                final ByteBuf[] currentWriteBufs = this.currentWriteBufs;
                final int maxLen = currentWriteBufs.length;
                int currentWriteBufsLen = this.currentWriteBufsLen;
                if (currentWriteBufsLen == 0) {
                    if (bufsSize > maxLen) {
                        for (int i = 0; i < maxLen; i++) {
                            currentWriteBufs[i] = bufs.get(i);
                        }
                        for (int i = maxLen; i < bufsSize; i++) {
                            writeBufs.offer(bufs.get(i));
                        }
                        this.currentWriteBufsLen = maxLen;
                    } else {
                        for (int i = 0; i < bufsSize; i++) {
                            currentWriteBufs[i] = bufs.get(i);
                        }
                        this.currentWriteBufsLen = bufsSize;
                    }
                } else {
                    final int currentRemain = maxLen - currentWriteBufsLen;
                    if (bufsSize > currentRemain) {
                        for (int i = 0; i < currentRemain; i++) {
                            currentWriteBufs[i + currentWriteBufsLen] = bufs.get(i);
                        }
                        for (int i = currentRemain; i < bufsSize; i++) {
                            writeBufs.offer(bufs.get(i));
                        }
                        this.currentWriteBufsLen = maxLen;
                    } else {
                        for (int i = 0; i < bufsSize; i++) {
                            currentWriteBufs[i + currentWriteBufsLen] = bufs.get(i);
                        }
                        this.currentWriteBufsLen += bufsSize;
                    }
                }
                try {
                    write();
                } catch (Throwable t) {
                    CloseUtil.close(this);
                }
            } else {
                if (maxWriteBacklog != Integer.MAX_VALUE) {
                    if (bufsSize + bufs.size() > maxWriteBacklog) {
                        ReleaseUtil.release(bufs);
                        close();
                        return;
                    }
                }
                for (ByteBuf buf : bufs) {
                    writeBufs.offer(buf);
                }
                try {
                    write();
                } catch (Throwable t) {
                    CloseUtil.close(this);
                }
            }
        } else {
            ReentrantLock lock = getCloseLock();
            lock.lock();
            try {
                if (isClosed()) {
                    ReleaseUtil.release(bufs);
                    return;
                }
                final Queue<ByteBuf> writeBufs = this.writeBufs;
                if (maxWriteBacklog != Integer.MAX_VALUE) {
                    if (writeBufs.size() + bufs.size() > maxWriteBacklog) {
                        ReleaseUtil.release(bufs);
                        close();
                        return;
                    }
                }
                for (ByteBuf buf : bufs) {
                    writeBufs.offer(buf);
                }
                if (writeBufs.size() != bufs.size()) {
                    return;
                }
            } catch (Exception e) {
                //will happen ?
                ReleaseUtil.release(bufs);
                CloseUtil.close(this);
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

    public Charset getCharset() {
        return getContext().getCharset();
    }

    public NioEventLoop getEventLoop() {
        return eventLoop;
    }

    public ExecutorEventLoop getExecutorEventLoop() {
        return executorEventLoop;
    }

    public IoEventHandle getIoEventHandle() {
        return ioEventHandle;
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

    public Object getProtocolId() {
        return protocolCodec.getProtocolId();
    }

    public Future getReadFuture() {
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

    public SSLEngine getSSLEngine() {
        return sslEngine;
    }

    public SslFuture getSslReadFuture() {
        return sslReadFuture;
    }

    public int getWriteBacklog() {
        //忽略current write[]
        return writeBufs.size();
    }

    @Override
    public int hashCode() {
        return remoteAddrPort.hashCode();
    }

    public boolean inEventLoop() {
        return eventLoop.inEventLoop();
    }

    private void interestRead(SelectionKey key) {
        if (SelectionKey.OP_READ != key.interestOps()) {
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

    public boolean isClosed() {
        return !opened;
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
        int length = channel.read(buf.nioBuffer());
        if (length < 1) {
            if (length == -1) {
                CloseUtil.close(this);
            }
            return;
        }
        buf.reverse();
        buf.flip();
        if (enableSsl) {
            FastThreadLocal l = FastThreadLocal.get();
            SslHandler sslHandler = l.getSslHandler();
            SslFuture sslTemporary = eventLoop.getSslTemporary();
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
                ByteBuf product = sslHandler.unwrap(this, future.getByteBuf());
                if (product == null) {
                    continue;
                }
                accept(product);
            }
        } else {
            accept(buf);
        }
    }

    public void readRemainingBuf(ByteBuf dst) {
        ByteBuf remainingBuf = this.remainingBuf;
        if (remainingBuf == null) {
            return;
        }
        dst.read(remainingBuf);
        remainingBuf.release(remainingBuf.getReleaseVersion());
        this.remainingBuf = null;
    }

    private void releaseBufs() {
        final ByteBuf[] cwbs = this.currentWriteBufs;
        final int maxLen = cwbs.length;
        // 这里有可能是因为异常关闭，currentWriteFutureLen不准确
        // 对所有不为空的future release
        for (int i = 0; i < maxLen; i++) {
            ByteBuf buf = cwbs[i];
            if (buf == null) {
                break;
            }
            ReleaseUtil.release(buf);
            cwbs[i] = null;
        }
        NioEventLoop eventLoop = this.eventLoop;
        ReleaseUtil.release(readFuture, eventLoop);
        ReleaseUtil.release(sslReadFuture, eventLoop);
        ReleaseUtil.release(remainingBuf);
        Queue<ByteBuf> wfs = this.writeBufs;
        if (wfs.size() == 0) {
            return;
        }
        ByteBuf buf = wfs.poll();
        for (; buf != null;) {
            ReleaseUtil.release(buf);
            buf = wfs.poll();
        }
    }

    public void setIoEventHandle(IoEventHandle ioEventHandle) {
        this.ioEventHandle = ioEventHandle;
    }

    public <T> void setOption(SocketOption<T> name, T value) throws IOException {
        channel.setOption(name, value);
    }

    public void setProtocolCodec(ProtocolCodec protocolCodec) {
        this.protocolCodec = protocolCodec;
    }

    public void setReadFuture(Future readFuture) {
        this.readFuture = readFuture;
    }

    public void setRemainingBuf(ByteBuf remainingBuf) {
        this.remainingBuf = remainingBuf;
    }

    public void setSslReadFuture(SslFuture future) {
        this.sslReadFuture = future;
    }

    @Override
    public String toString() {
        return channelDesc;
    }

    protected void write() throws IOException {
        final NioEventLoop eventLoop = this.eventLoop;
        final Queue<ByteBuf> writeBufs = this.writeBufs;
        final SelectionKey selectionKey = this.selectionKey;
        final ByteBuf[] currentWriteBufs = this.currentWriteBufs;
        final ByteBuffer[] writeBuffers = eventLoop.getWriteBuffers();
        final int maxLen = currentWriteBufs.length;
        for (;;) {
            int currentWriteBufsLen = this.currentWriteBufsLen;
            for (; currentWriteBufsLen < maxLen;) {
                ByteBuf buf = writeBufs.poll();
                if (buf == null) {
                    break;
                }
                currentWriteBufs[currentWriteBufsLen++] = buf;
            }
            if (currentWriteBufsLen == 0) {
                interestRead(selectionKey);
                return;
            }
            //FIXME ...是否要清空buffers
            for (int i = 0; i < currentWriteBufsLen; i++) {
                ByteBuf buf = currentWriteBufs[i];
                writeBuffers[i] = buf.nioBuffer();
            }
            if (currentWriteBufsLen == 1) {
                ByteBuffer nioBuf = writeBuffers[0];
                channel.write(nioBuf);
                if (nioBuf.hasRemaining()) {
                    this.currentWriteBufsLen = 1;
                    currentWriteBufs[0].reverse();
                    interestWrite(selectionKey);
                    return;
                } else {
                    ByteBuf buf = currentWriteBufs[0];
                    currentWriteBufs[0] = null;
                    buf.release(buf.getReleaseVersion());
                    this.currentWriteBufsLen = 0;
                    interestRead(selectionKey);
                    return;
                }
            } else {
                channel.write(writeBuffers, 0, currentWriteBufsLen);
                for (int i = 0; i < currentWriteBufsLen; i++) {
                    ByteBuf buf = currentWriteBufs[i];
                    if (writeBuffers[i].hasRemaining()) {
                        int remain = currentWriteBufsLen - i;
                        if (remain > 16) {
                            System.arraycopy(currentWriteBufs, i, currentWriteBufs, 0, remain);
                        } else {
                            for (int j = 0; j < remain; j++) {
                                currentWriteBufs[j] = currentWriteBufs[i + j];
                            }
                        }
                        for (int j = currentWriteBufsLen - i; j < maxLen; j++) {
                            currentWriteBufs[j] = null;
                        }
                        buf.reverse();
                        this.currentWriteBufsLen = remain;
                        interestWrite(selectionKey);
                        return;
                    } else {
                        buf.release(buf.getReleaseVersion());
                    }
                }
                for (int j = 0; j < currentWriteBufsLen; j++) {
                    currentWriteBufs[j] = null;
                }
                this.currentWriteBufsLen = 0;
                if (currentWriteBufsLen != maxLen) {
                    interestRead(selectionKey);
                    return;
                }
            }
        }
    }

    private void write(ByteBuf buf) {
        try {
            channel.write(buf.nioBuffer());
            buf.reverse();
            if (buf.hasRemaining()) {
                currentWriteBufsLen = 1;
                currentWriteBufs[0] = buf;
                interestWrite(selectionKey);
                return;
            } else {
                buf.release(buf.getReleaseVersion());
                interestRead(selectionKey);
            }
        } catch (Exception e) {
            ReleaseUtil.release(buf);
            CloseUtil.close(this);
        }
    }

}

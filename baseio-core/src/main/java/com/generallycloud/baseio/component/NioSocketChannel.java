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

import static com.generallycloud.baseio.Develop.printException;
import static com.generallycloud.baseio.common.ThrowableUtil.unknownStackTrace;

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
import com.generallycloud.baseio.common.Assert;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.ReleaseUtil;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.ChannelContext.HeartBeatLogger;
import com.generallycloud.baseio.component.ssl.SslHandler;
import com.generallycloud.baseio.concurrent.ExecutorEventLoop;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.baseio.protocol.Frame;
import com.generallycloud.baseio.protocol.ProtocolCodec;

public final class NioSocketChannel extends AttributesImpl
        implements Runnable, Attributes, Closeable {

    private static final int                    SSL_PACKET_LIMIT     = 1024 * 64;
    private static final ClosedChannelException CLOSED_WHEN_FLUSH    = unknownStackTrace(
            new ClosedChannelException(), NioSocketChannel.class, "flush(...)");
    private static final SSLException           NOT_TLS              = unknownStackTrace(
            new SSLException("NOT TLS"), NioSocketChannel.class, "isEnoughSslUnwrap()");
    private static final SSLException           SSL_OVER_LIMIT       = unknownStackTrace(
            new SSLException("over limit (" + SSL_PACKET_LIMIT + ")"), NioSocketChannel.class,
            "isEnoughSslUnwrap()");
    private static final InetSocketAddress      ERROR_SOCKET_ADDRESS = new InetSocketAddress(0);
    private static final Logger                 logger               = LoggerFactory
            .getLogger(NioSocketChannel.class);
    private final SocketChannel                 channel;
    private final Integer                       channelId;
    private final ReentrantLock                 closeLock            = new ReentrantLock();
    private final ChannelContext                context;
    private final long                          creationTime         = System.currentTimeMillis();
    private final ByteBuf[]                     currentWriteBufs;
    private int                                 currentWriteBufsLen;
    private final String                        desc;
    private final boolean                       enableSsl;
    private final NioEventLoop                  eventLoop;
    private final ExecutorEventLoop             executorEventLoop;
    private IoEventHandle                       ioEventHandle;
    private long                                lastAccess;
    private final String                        localAddr;
    private final int                           localPort;
    private final int                           maxWriteBacklog;
    private boolean                             opened               = true;
    private ByteBuf                             plainRemainBuf;
    private ProtocolCodec                       codec;
    private Frame                               readFrame;
    private final String                        remoteAddr;
    private final String                        remoteAddrPort;
    private final int                           remotePort;
    private final SelectionKey                  selKey;
    private final SSLEngine                     sslEngine;
    private boolean                             sslHandshakeFinished;
    private ByteBuf                             sslRemainBuf;
    private byte                                sslWrapExt;
    private final Queue<ByteBuf>                writeBufs;

    NioSocketChannel(NioEventLoop eventLoop, SelectionKey selectionKey, ChannelContext context,
            int channelId) {
        NioEventLoopGroup group = eventLoop.getGroup();
        this.context = context;
        this.selKey = selectionKey;
        this.eventLoop = eventLoop;
        this.channelId = channelId;
        this.enableSsl = context.isEnableSsl();
        this.codec = context.getProtocolCodec();
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
        this.desc = new StringBuilder("[Id(0x").append(StringUtil.getZeroString(8 - idhex.length()))
                .append(idhex).append(")R/").append(getRemoteAddr()).append(":")
                .append(getRemotePort()).append("; L:").append(getLocalPort()).append("]")
                .toString();
        if (context.isEnableSsl()) {
            this.sslEngine = context.getSslContext().newEngine(remoteAddr, remotePort);
        } else {
            this.sslEngine = null;
        }
    }

    private void accept(ByteBuf src) throws IOException {
        final ByteBufAllocator alloc = alloc();
        final ProtocolCodec codec = this.codec;
        final IoEventHandle eventHandle = this.ioEventHandle;
        final HeartBeatLogger heartBeatLogger = context.getHeartBeatLogger();
        final boolean enableWorkEventLoop = context.isEnableWorkEventLoop();
        Frame frame = readFrame;
        if (frame == null) {
            frame = codec.decode(this, src);
        }
        for (;;) {
            if (!frame.read(this, src)) {
                readFrame = frame;
                if (src.hasRemaining()) {
                    ByteBuf remaining = alloc.allocate(src.remaining());
                    remaining.read(src);
                    remaining.flip();
                    plainRemainBuf = remaining;
                }
                break;
            }
            if (frame.isSilent()) {
                if (frame.isPing()) {
                    heartBeatLogger.logPing(this);
                    Frame f = codec.pong(this, frame);
                    if (f != null) {
                        flush(f);
                    }
                } else if (frame.isPong()) {
                    heartBeatLogger.logPong(this);
                }
            } else {
                if (enableWorkEventLoop) {
                    accept(eventHandle, frame);
                } else {
                    try {
                        eventHandle.accept(this, frame);
                    } catch (Exception e) {
                        eventHandle.exceptionCaught(this, frame, e);
                    }
                }
            }
            if (!src.hasRemaining()) {
                readFrame = null;
                break;
            }
            frame = codec.decode(this, src);
        }
    }

    private void accept(final IoEventHandle eventHandle, final Frame frame) {
        getExecutorEventLoop().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    eventHandle.accept(NioSocketChannel.this, frame);
                } catch (Exception e) {
                    eventHandle.exceptionCaught(NioSocketChannel.this, frame, e);
                }
            }
        });
    }

    public ByteBufAllocator alloc() {
        return eventLoop.alloc();
    }

    @Override
    public void close() {
        if (isClosed()) {
            return;
        }
        if (inEventLoop()) {
            safeClose();
        } else {
            execute(new CloseEvent(this));
        }
    }

    private void closeSsl() {
        if (enableSsl) {
            if (!channel.isOpen()) {
                return;
            }
            sslEngine.closeOutbound();
            if (context.getSslContext().isClient()) {
                try {
                    writeBufs.offer(sslHandler().wrap(this, EmptyByteBuf.get()));
                    write(selKey.interestOps());
                } catch (Exception e) {}
            }
            try {
                sslEngine.closeInbound();
            } catch (Exception e) {}
        }
    }

    private void execute(Runnable event) {
        eventLoop.execute(event);
    }

    public ByteBuf encode(Frame frame) throws IOException {
        return codec.encode(this, frame);
    }

    private void exceptionCaught(Frame frame, Exception ex) {
        frame.release(eventLoop);
        try {
            getIoEventHandle().exceptionCaught(this, frame, ex);
        } catch (Throwable e) {
            printException(logger, e);
            printException(logger, ex);
        }
    }

    @SuppressWarnings("resource")
    protected void finishHandshake() throws IOException {
        sslHandshakeFinished = true;
        if (context.getSslContext().isClient()) {
            ChannelService service = context.getChannelService();
            ChannelConnector connector = (ChannelConnector) service;
            connector.finishConnect(this, null);
        }
        fireOpend();
    }

    private void fireClosed() {
        final NioSocketChannel ch = this;
        eventLoop.removeChannel(ch);
        for (ChannelEventListener l : context.getChannelEventListeners()) {
            try {
                l.channelClosed(ch);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    protected void fireOpend() {
        final NioSocketChannel ch = this;
        for (ChannelEventListener l : context.getChannelEventListeners()) {
            try {
                l.channelOpened(ch);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                CloseUtil.close(ch);
                return;
            }
        }
        if (ioEventHandle == null) {
            ioEventHandle = context.getIoEventHandle();
        }
    }

    public void flush(ByteBuf buf) {
        Assert.notNull(buf, "null buf");
        if (enableSsl) {
            ByteBuf old = buf;
            try {
                buf = sslHandler().wrap(this, old);
            } catch (Exception e) {
                printException(logger, e);
            } finally {
                old.release();
            }
        }
        final Queue<ByteBuf> writeBufs = this.writeBufs;
        if (inEventLoop()) {
            if (isClosed()) {
                buf.release();
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
                    write(selKey.interestOps());
                } catch (Throwable t) {
                    close();
                }
            }
        } else {
            ReentrantLock lock = getCloseLock();
            lock.lock();
            try {
                if (isClosed()) {
                    buf.release();
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
            eventLoop.dispatchChannel(this);
        }
    }

    public void flush(Frame frame) {
        Assert.notNull(frame, "null frame");
        if (isClosed()) {
            exceptionCaught(frame, CLOSED_WHEN_FLUSH);
            return;
        }
        ByteBuf buf = null;
        try {
            buf = codec.encode(this, frame);
            frame.flush();
            frame.release(eventLoop);
        } catch (Exception e) {
            ReleaseUtil.release(buf);
            exceptionCaught(frame, e);
            return;
        }
        flush(buf);
    }

    //FIXME ..使用该方法貌似会性能下降？查找原因
    public void flush(List<ByteBuf> bufs) {
        if (bufs != null && !bufs.isEmpty()) {
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
                        write(selKey.interestOps());
                    } catch (Throwable t) {
                        close();
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
                        write(selKey.interestOps());
                    } catch (Throwable t) {
                        close();
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
                eventLoop.dispatchChannel(this);
            }
        }
    }

    public Integer getChannelId() {
        return channelId;
    }

    public Charset getCharset() {
        return context.getCharset();
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

    public ProtocolCodec getCodec() {
        return codec;
    }

    public String getCodecId() {
        return codec.getProtocolId();
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

    protected byte getSslWrapExt() {
        return sslWrapExt;
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

    private void interestRead(SelectionKey key, int interestOps) {
        if (SelectionKey.OP_READ != interestOps) {
            key.interestOps(SelectionKey.OP_READ);
        }
    }

    private void interestWrite(SelectionKey key, int interestOps) {
        if ((SelectionKey.OP_READ | SelectionKey.OP_WRITE) != interestOps) {
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

    protected boolean isSslHandshakeFinished() {
        return sslHandshakeFinished;
    }

    /**
     <pre>
               record type (1 byte)
            /
           /    version (1 byte major, 1 byte minor)
          /    /
         /    /         length (2 bytes)
        /    /         /
     +----+----+----+----+----+
     |    |    |    |    |    |
     |    |    |    |    |    | TLS Record header
     +----+----+----+----+----+
    
    
     Record Type Values       dec      hex
     -------------------------------------
     CHANGE_CIPHER_SPEC        20     0x14
     ALERT                     21     0x15
     HANDSHAKE                 22     0x16
     APPLICATION_DATA          23     0x17
    
    
     Version Values            dec     hex
     -------------------------------------
     SSL 3.0                   3,0  0x0300
     TLS 1.0                   3,1  0x0301
     TLS 1.1                   3,2  0x0302
     TLS 1.2                   3,3  0x0303
     
     ref:http://blog.fourthbit.com/2014/12/23/traffic-analysis-of-an-ssl-slash-tls-session/
     </pre>
    */
    private boolean isEnoughSslUnwrap(ByteBuf src) throws SSLException {
        if (src.remaining() < 5) {
            return false;
        }
        int pos = src.position();
        // TLS - Check ContentType
        int type = src.getUnsignedByte(pos);
        if (type < 20 || type > 23) {
            throw NOT_TLS;
        }
        // TLS - Check ProtocolVersion
        int majorVersion = src.getUnsignedByte(pos + 1);
        int minorVersion = src.getUnsignedByte(2);
        int packetLength = src.getUnsignedShort(pos + 3);
        if (majorVersion != 3 || minorVersion < 1) {
            // NOT TLS (i.e. SSLv2,3 or bad data)
            throw NOT_TLS;
        }
        int len = packetLength + 5;
        if (src.remaining() < len) {
            return false;
        }
        if (len > SSL_PACKET_LIMIT) {
            throw SSL_OVER_LIMIT;
        }
        src.markL();
        src.limit(pos + len);
        return true;
    }

    protected void read(ByteBuf src) throws IOException {
        lastAccess = System.currentTimeMillis();
        src.clear();
        if (enableSsl) {
            readSslPlainRemainingBuf(src);
        } else {
            readPlainRemainingBuf(src);
        }
        int length = channel.read(src.nioBuffer());
        if (length < 1) {
            if (length == -1) {
                CloseUtil.close(this);
            }
            return;
        }
        src.reverse();
        src.flip();
        if (enableSsl) {
            SslHandler sslHandler = sslHandler();
            for (;;) {
                if (isEnoughSslUnwrap(src)) {
                    ByteBuf res = sslHandler.unwrap(this, src);
                    if (res != null) {
                        accept(res);
                    }
                    src.resetL();
                    if (!src.hasRemaining()) {
                        return;
                    }
                } else {
                    if (src.hasRemaining()) {
                        int remain = src.remaining();
                        ByteBuf remaining = alloc().allocate(remain);
                        remaining.read(src);
                        remaining.flip();
                        sslRemainBuf = remaining;
                    }
                    return;
                }
            }
        } else {
            accept(src);
        }
    }

    protected void readPlainRemainingBuf(ByteBuf dst) {
        ByteBuf remainingBuf = this.plainRemainBuf;
        if (remainingBuf == null) {
            return;
        }
        dst.read(remainingBuf);
        remainingBuf.release();
        this.plainRemainBuf = null;
    }

    protected void readSslPlainRemainingBuf(ByteBuf dst) {
        ByteBuf remainingBuf = this.sslRemainBuf;
        if (remainingBuf == null) {
            return;
        }
        dst.read(remainingBuf);
        remainingBuf.release();
        this.sslRemainBuf = null;
    }

    private void releaseBufs() {
        final ByteBuf[] cwbs = this.currentWriteBufs;
        final int maxLen = cwbs.length;
        // 这里有可能是因为异常关闭，currentWriteFrameLen不准确
        // 对所有不为空的frame release
        for (int i = 0; i < maxLen; i++) {
            ByteBuf buf = cwbs[i];
            if (buf == null) {
                break;
            }
            buf.release();
            cwbs[i] = null;
        }
        NioEventLoop eventLoop = this.eventLoop;
        ReleaseUtil.release(readFrame, eventLoop);
        ReleaseUtil.release(sslRemainBuf);
        ReleaseUtil.release(plainRemainBuf);
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

    @Override
    public void run() {
        if (isClosed()) {
            return;
        }
        try {
            write(selKey.interestOps());
        } catch (Exception e) {
            close();
        }
    }

    private void safeClose() {
        if (isClosed()) {
            return;
        }
        ReentrantLock lock = getCloseLock();
        lock.lock();
        try {
            if (isOpened()) {
                opened = false;
                closeSsl();
                releaseBufs();
                CloseUtil.close(channel);
                selKey.attach(null);
                selKey.cancel();
                fireClosed();
            }
        } finally {
            lock.unlock();
        }
    }

    public void setIoEventHandle(IoEventHandle ioEventHandle) {
        this.ioEventHandle = ioEventHandle;
    }

    public <T> void setOption(SocketOption<T> name, T value) throws IOException {
        channel.setOption(name, value);
    }

    public void setCodec(ProtocolCodec codec) {
        this.codec = codec;
    }

    protected void setSslWrapExt(byte sslWrapExt) {
        this.sslWrapExt = sslWrapExt;
    }

    protected SslHandler sslHandler() {
        return FastThreadLocal.get().getSslHandler();
    }

    @Override
    public String toString() {
        return desc;
    }

    private void write(ByteBuf buf) {
        try {
            channel.write(buf.nioBuffer());
            buf.reverse();
            int interestOps = selKey.interestOps();
            if (buf.hasRemaining()) {
                currentWriteBufsLen = 1;
                currentWriteBufs[0] = buf;
                interestWrite(selKey, interestOps);
                return;
            } else {
                buf.release();
                interestRead(selKey, interestOps);
            }
        } catch (Exception e) {
            ReleaseUtil.release(buf);
            CloseUtil.close(this);
        }
    }

    protected boolean write(final int interestOps) throws IOException {
        final NioEventLoop eventLoop = this.eventLoop;
        final Queue<ByteBuf> writeBufs = this.writeBufs;
        final SelectionKey selectionKey = this.selKey;
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
                interestRead(selectionKey, interestOps);
                return true;
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
                    interestWrite(selectionKey, interestOps);
                    return false;
                } else {
                    ByteBuf buf = currentWriteBufs[0];
                    currentWriteBufs[0] = null;
                    buf.release();
                    this.currentWriteBufsLen = 0;
                    interestRead(selectionKey, interestOps);
                    return true;
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
                        interestWrite(selectionKey, interestOps);
                        return false;
                    } else {
                        buf.release();
                    }
                }
                for (int j = 0; j < currentWriteBufsLen; j++) {
                    currentWriteBufs[j] = null;
                }
                this.currentWriteBufsLen = 0;
                if (currentWriteBufsLen != maxLen) {
                    interestRead(selectionKey, interestOps);
                    return true;
                }
            }
        }
    }

    class CloseEvent implements Runnable, Closeable {

        final NioSocketChannel ch;

        public CloseEvent(NioSocketChannel ch) {
            this.ch = ch;
        }

        @Override
        public void close() throws IOException {
            ch.safeClose();
        }

        @Override
        public void run() {
            ch.safeClose();
        }

    }

}

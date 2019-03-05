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

import static com.firenio.baseio.Develop.printException;
import static com.firenio.baseio.common.Util.unknownStackTrace;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;
import javax.net.ssl.SSLException;

import com.firenio.baseio.Develop;
import com.firenio.baseio.buffer.ByteBuf;
import com.firenio.baseio.buffer.ByteBufAllocator;
import com.firenio.baseio.common.Unsafe;
import com.firenio.baseio.common.Util;
import com.firenio.baseio.component.NioEventLoop.EpollNioEventLoopUnsafe;
import com.firenio.baseio.component.NioEventLoop.JavaNioEventLoopUnsafe;
import com.firenio.baseio.component.NioEventLoop.NioEventLoopUnsafe;
import com.firenio.baseio.concurrent.EventLoop;
import com.firenio.baseio.log.Logger;
import com.firenio.baseio.log.LoggerFactory;

//请勿使用remote.getRemoteHost(),可能出现阻塞
public final class Channel implements Runnable, Closeable {

    public static final ClosedChannelException CLOSED_WHEN_FLUSH     = CLOSED_WHEN_FLUSH();
    public static final InetSocketAddress      ERROR_SOCKET_ADDRESS  = new InetSocketAddress(0);
    public static final Logger                 logger                = newLogger();
    public static final SSLException           NOT_TLS               = NOT_TLS();
    public static final int                    SSL_PACKET_LIMIT      = 1024 * 64;
    public static final SSLException           SSL_PACKET_OVER_LIMIT = SSL_PACKET_OVER_LIMIT();
    public static final SSLException           SSL_UNWRAP_OVER_LIMIT = SSL_UNWRAP_OVER_LIMIT();
    public static final IOException            TAST_REJECT           = TASK_REJECT();

    private Object                             attachment;
    private ProtocolCodec                      codec;
    private final ChannelContext               context;
    private final long                         creationTime          = System.currentTimeMillis();
    private final ByteBuf[]                    currentWriteBufs;
    private int                                currentWriteBufsLen;
    private final String                       desc;
    private final boolean                      enableSsl;
    private final NioEventLoop                 eventLoop;
    private final EventLoop                    executorEventLoop;
    private boolean                            inEvent;
    private long                               lastAccess;
    private volatile boolean                   open                  = true;
    private ByteBuf                            plainRemainBuf;
    private final SSLEngine                    sslEngine;
    private boolean                            sslHandshakeFinished;
    private ByteBuf                            sslRemainBuf;
    private byte                               sslWrapExt;
    private final ChannelUnsafe                unsafe;
    private final Queue<ByteBuf>               writeBufs;

    Channel(NioEventLoop el, ChannelContext ctx, ChannelUnsafe unsafe) {
        this.context = ctx;
        this.eventLoop = el;
        this.unsafe = unsafe;
        this.enableSsl = ctx.isEnableSsl();
        this.codec = ctx.getDefaultCodec();
        this.executorEventLoop = ctx.getNextExecutorEventLoop();
        this.lastAccess = creationTime + el.getGroup().getIdleTime();
        this.writeBufs = new LinkedBlockingQueue<>();
        this.currentWriteBufs = new ByteBuf[el.getGroup().getWriteBuffers()];
        String idhex = Integer.toHexString(unsafe.channelId);
        this.desc = newDesc(idhex);
        if (ctx.isEnableSsl()) {
            this.sslEngine = ctx.getSslContext().newEngine(getRemoteAddr(), getRemotePort());
        } else {
            this.sslHandshakeFinished = true;
            this.sslEngine = null;
        }
    }

    private void accept(ByteBuf src) throws Exception {
        final ProtocolCodec codec = getCodec();
        final IoEventHandle handle = getIoEventHandle();
        final boolean enable_wel = getExecutorEventLoop() != null;
        for (;;) {
            Frame f = codec.decode(this, src);
            if (f == null) {
                if (Develop.BUF_DEBUG) {
                    if (plainRemainBuf != null) {
                        throw new Exception("plainRemainBuf not null");
                    }
                }
                plainRemainBuf = sliceRemain(src);
                break;
            }
            if (f.isTyped()) {
                accept_typed(f);
            } else {
                if (enable_wel) {
                    accept_async(f);
                } else {
                    accept_line(handle, f);
                }
            }
            if (!src.hasRemaining()) {
                break;
            }
        }
    }

    private void accept_async(final Frame f) {
        final EventLoop executorEventLoop = getExecutorEventLoop();
        final Runnable job = new Runnable() {

            @Override
            public void run() {
                final Channel ch = Channel.this;
                try {
                    ch.getIoEventHandle().accept(ch, f);
                } catch (Exception e) {
                    ch.getIoEventHandle().exceptionCaught(ch, f, e);
                }
            }
        };
        if (!executorEventLoop.submit(job)) {
            exceptionCaught(f, TAST_REJECT);
        }
    }

    private void accept_line(IoEventHandle handle, Frame frame) {
        try {
            handle.accept(this, frame);
        } catch (Exception e) {
            exceptionCaught(frame, e);
        }
    }

    private void accept_typed(Frame f) throws Exception {
        if (f.isPing()) {
            context.getHeartBeatLogger().logPing(this);
            Frame pong = codec.pong(this, f);
            if (pong != null) {
                writeAndFlush(pong);
            }
        } else if (f.isPong()) {
            context.getHeartBeatLogger().logPong(this);
        }
    }

    public ByteBufAllocator alloc() {
        return eventLoop.alloc();
    }

    public ByteBuf allocate() {
        return alloc().allocate().skip(codec.headerLength());
    }

    public ByteBuf allocate(int limit) {
        int h = codec.headerLength();
        return alloc().allocate(h + limit).skip(h);
    }

    private void check_write_overflow() {
        if (writeBufs.size() > context.getMaxWriteBacklog()) {
            safeClose();
        }
    }

    @Override
    public void close() {
        if (inEventLoop()) {
            safeClose();
        } else {
            if (isOpen()) {
                eventLoop.submit(new CloseEvent(this));
            }
        }
    }

    private void closeSsl() {
        if (enableSsl) {
            sslEngine.closeOutbound();
            if (context.getSslContext().isClient()) {
                try {
                    writeBufs.offer(wrap(ByteBuf.empty()));
                    write(eventLoop.getUnsafe());
                } catch (Exception e) {}
            }
            try {
                sslEngine.closeInbound();
            } catch (Exception e) {}
        }
    }

    public ByteBuf encode(Frame frame) throws Exception {
        return codec.encode(this, frame);
    }

    private void exceptionCaught(Frame frame, Exception ex) {
        try {
            getIoEventHandle().exceptionCaught(this, frame, ex);
        } catch (Throwable e) {
            printException(logger, e, 2);
            printException(logger, ex, 2);
        }
    }

    private void finishHandshake() {
        this.sslHandshakeFinished = true;
        this.fireOpened();
        this.context.channelEstablish(this, null);
    }

    private void fireClosed() {
        eventLoop.removeChannel(unsafe.channelId);
        List<ChannelEventListener> ls = context.getChannelEventListeners();
        for (int i = 0, count = ls.size(); i < count; i++) {
            ChannelEventListener l = ls.get(i);
            try {
                l.channelClosed(this);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    protected void fireOpened() {
        setAttachment(codec.newAttachment());
        List<ChannelEventListener> ls = context.getChannelEventListeners();
        for (int i = 0, count = ls.size(); i < count; i++) {
            ChannelEventListener l = ls.get(i);
            try {
                l.channelOpened(this);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                Util.close(this);
                return;
            }
            if (!this.isOpen()) {
                return;
            }
        }
    }

    public void flush() {
        if (inEventLoop()) {
            if (!inEvent) {
                inEvent = true;
                eventLoop.getJobs().offer(this);
            }
        } else {
            eventLoop.submit(this);
        }
    }

    public Object getAttachment() {
        return attachment;
    }

    public Integer getChannelId() {
        return unsafe.channelId;
    }

    public Charset getCharset() {
        return context.getCharset();
    }

    public ProtocolCodec getCodec() {
        return codec;
    }

    public String getCodecId() {
        return codec.getProtocolId();
    }

    public ChannelContext getContext() {
        return context;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public String getDesc() {
        return desc;
    }

    public NioEventLoop getEventLoop() {
        return eventLoop;
    }

    public EventLoop getExecutorEventLoop() {
        return executorEventLoop;
    }

    public IoEventHandle getIoEventHandle() {
        return context.getIoEventHandle();
    }

    public long getLastAccessTime() {
        return lastAccess;
    }

    public int getLocalPort() {
        return unsafe.localPort;
    }

    public int getOption(int name) throws IOException {
        return unsafe.getOption(name);
    }

    public String getRemoteAddr() {
        return unsafe.remoteAddr;
    }

    public int getRemotePort() {
        return unsafe.remotePort;
    }

    public SSLEngine getSSLEngine() {
        return sslEngine;
    }

    public int getWriteBacklog() {
        //忽略current write[]
        return writeBufs.size();
    }

    //FIXME not correct ,fix this
    private int guessWrapOut(int src, int ext) {
        if (Develop.BUF_DEBUG) {
            return 1;
        } else {
            if (SslContext.OPENSSL_AVAILABLE) {
                return ((src + SslContext.SSL_PACKET_BUFFER_SIZE - 1)
                        / SslContext.SSL_PACKET_BUFFER_SIZE + 1) * ext + src;
            } else {
                return ((src + SslContext.SSL_PACKET_BUFFER_SIZE - 1)
                        / SslContext.SSL_PACKET_BUFFER_SIZE)
                        * (ext + SslContext.SSL_PACKET_BUFFER_SIZE);
            }
        }
    }

    @Override
    public int hashCode() {
        return desc.hashCode();
    }

    public boolean inEventLoop() {
        return eventLoop.inEventLoop();
    }

    public boolean isCodec(String codecId) {
        return codec.getProtocolId().equals(codecId);
    }

    public boolean isEnableSsl() {
        return enableSsl;
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
        int minorVersion = src.getUnsignedByte(pos + 2);
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
            throw SSL_PACKET_OVER_LIMIT;
        }
        src.markL();
        src.limit(pos + len);
        return true;
    }

    public boolean isOpen() {
        return open;
    }

    protected boolean isSslHandshakeFinished() {
        return sslHandshakeFinished;
    }

    private String newDesc(String idhex) {
        StringBuilder sb = FastThreadLocal.get().getStringBuilder();
        sb.append("[id(0x");
        sb.append(idhex);
        sb.append(")R/");
        sb.append(getRemoteAddr());
        sb.append(':');
        sb.append(getRemotePort());
        sb.append("; L:");
        sb.append(getLocalPort());
        sb.append("]");
        return sb.toString();
    }

    protected void read() throws Exception {
        lastAccess = System.currentTimeMillis();
        if (enableSsl) {
            read_ssl();
        } else {
            read_plain();
        }
    }

    private void read_plain() throws Exception {
        NioEventLoop el = eventLoop;
        ByteBuf src = el.getReadBuf();
        for (;;) {
            src.clear();
            readPlainRemainingBuf(src);
            int length = unsafe.read(el);
            if (length < 1) {
                if (length == -1) {
                    Util.close(this);
                    return;
                }
                if (src.position() > 0) {
                    src.flip();
                    plainRemainBuf = sliceRemain(src);
                }
                return;
            }
            if (Native.EPOLL_AVAIABLE) {
                src.absLimit(src.absPos() + length);
                src.absPos(0);
            } else {
                src.reverse();
                src.flip();
            }
            boolean b = src.absLimit() != src.capacity();
            accept(src);
            if (b) {
                break;
            }
        }
    }

    private void read_ssl() throws Exception {
        NioEventLoop el = eventLoop;
        ByteBuf src = el.getReadBuf();
        for (;;) {
            src.clear();
            readSslRemainingBuf(src);
            int length = unsafe.read(el);
            if (length < 1) {
                if (length == -1) {
                    Util.close(this);
                    return;
                }
                if (src.position() > 0) {
                    src.flip();
                    sslRemainBuf = sliceRemain(src);
                }
                return;
            }
            if (Native.EPOLL_AVAIABLE) {
                src.absLimit(src.absPos() + length);
                src.absPos(0);
            } else {
                src.reverse();
                src.flip();
            }
            boolean b = src.absLimit() != src.capacity();
            for (;;) {
                if (isEnoughSslUnwrap(src)) {
                    ByteBuf res = unwrap(src);
                    if (res != null) {
                        accept(res);
                    }
                    src.resetL();
                    if (!src.hasRemaining()) {
                        break;
                    }
                } else {
                    if (src.hasRemaining()) {
                        sslRemainBuf = sliceRemain(src);
                    }
                    break;
                }
            }
            if (b) {
                break;
            }
        }
    }

    private void readPlainRemainingBuf(ByteBuf dst) {
        ByteBuf remainingBuf = this.plainRemainBuf;
        if (remainingBuf == null) {
            return;
        }
        dst.put(remainingBuf);
        remainingBuf.release();
        this.plainRemainBuf = null;
    }

    private void readSslRemainingBuf(ByteBuf dst) {
        ByteBuf remainingBuf = this.sslRemainBuf;
        if (remainingBuf == null) {
            return;
        }
        dst.put(remainingBuf);
        remainingBuf.release();
        this.sslRemainBuf = null;
    }

    public void release(Frame frame) {
        codec.release(eventLoop, frame);
    }

    private void releaseWriteBufArray() {
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
    }

    private void releaseWriteBufQueue() {
        Queue<ByteBuf> wfs = this.writeBufs;
        if (!wfs.isEmpty()) {
            ByteBuf buf = wfs.poll();
            for (; buf != null;) {
                Util.release(buf);
                buf = wfs.poll();
            }
        }
    }

    private void removeChannel() {
        Integer id = getChannelId();
        context.getChannelManager().removeChannel(id);
        eventLoop.removeChannel(id.intValue());
    }

    @Override
    public void run() {
        if (isOpen()) {
            inEvent = false;
            if (unsafe.interestWrite()) {
                // check write over flow
                check_write_overflow();
            } else {
                if (write(eventLoop.getUnsafe()) == -1) {
                    safeClose();
                }
            }
        }
    }

    private void runDelegatedTasks(SSLEngine engine) {
        for (;;) {
            Runnable task = engine.getDelegatedTask();
            if (task == null) {
                break;
            }
            task.run();
        }
    }

    private void safeClose() {
        if (isOpen()) {
            open = false;
            closeSsl();
            releaseWriteBufArray();
            releaseWriteBufQueue();
            removeChannel();
            Util.release(sslRemainBuf);
            Util.release(plainRemainBuf);
            Util.close(unsafe);
            fireClosed();
            stopContext();
        }
    }

    public void setAttachment(Object attachment) {
        this.attachment = attachment;
    }

    public void setCodec(String codecId) throws IOException {
        if (inEventLoop()) {
            this.codec = context.getProtocolCodec(codecId);
        } else {
            //FIXME .. is this work?
            synchronized (this) {
                this.codec = context.getProtocolCodec(codecId);
            }
        }
    }

    public void setOption(int name, int value) throws IOException {
        unsafe.setOption(name, value);
    }

    private ByteBuf sliceRemain(ByteBuf src) {
        int remain = src.remaining();
        if (remain > 0) {
            ByteBuf remaining = alloc().allocate(remain);
            remaining.put(src);
            return remaining.flip();
        } else {
            return null;
        }
    }

    private void stopContext() {
        if (context instanceof ChannelConnector) {
            Util.close(((ChannelConnector) context));
        }
    }

    //FIXME 部分buf不需要swap
    private ByteBuf swap(ByteBufAllocator allocator, ByteBuf buf) {
        ByteBuf out = allocator.allocate(buf.limit());
        out.put(buf);
        return out.flip();
    }

    private void synchByteBuf(SSLEngineResult result, ByteBuf src, ByteBuf dst) {
        //FIXME 同步。。。。。
        src.reverse();
        dst.reverse();
        //      int bytesConsumed = result.bytesConsumed();
        //      int bytesProduced = result.bytesProduced();
        //      
        //      if (bytesConsumed > 0) {
        //          src.skipBytes(bytesConsumed);
        //      }
        //
        //      if (bytesProduced > 0) {
        //          dst.skipBytes(bytesProduced);
        //      }
    }

    @Override
    public String toString() {
        return desc;
    }

    private ByteBuf unwrap(ByteBuf src) throws IOException {
        SSLEngine sslEngine = getSSLEngine();
        ByteBuf dst = FastThreadLocal.get().getSslUnwrapBuf();
        if (sslHandshakeFinished) {
            dst.clear();
            readPlainRemainingBuf(dst);
            SSLEngineResult result = sslEngine.unwrap(src.nioBuffer(), dst.nioBuffer());
            if (result.getStatus() == Status.BUFFER_OVERFLOW) {
                //why throw an exception here instead of handle it?
                //the getSslUnwrapBuf will return an thread local buffer for unwrap,
                //the buffer's size defined by Constants.SSL_UNWRAP_BUFFER_SIZE_KEY in System property
                //or default value 256KB(1024 * 256), although the buffer will not occupy so much memory because
                //one EventLoop only have one buffer,but before do unwrap, every channel maybe cached a large
                //buffer under SSL_UNWRAP_BUFFER_SIZE,I do not think it is a good way to cached much memory in
                //channel, it is not friendly for load much channels in one system, if you get exception here,
                //you may need find a way to limit you frame size,or cache your incomplete frame's data to
                //file system or others way.
                throw SSL_UNWRAP_OVER_LIMIT;
            }
            synchByteBuf(result, src, dst);
            return dst.flip();
        } else {
            for (;;) {
                dst.clear();
                SSLEngineResult result = sslEngine.unwrap(src.nioBuffer(), dst.nioBuffer());
                HandshakeStatus handshakeStatus = result.getHandshakeStatus();
                synchByteBuf(result, src, dst);
                if (handshakeStatus == HandshakeStatus.NEED_WRAP) {
                    writeAndFlush(ByteBuf.empty());
                    return null;
                } else if (handshakeStatus == HandshakeStatus.NEED_TASK) {
                    runDelegatedTasks(sslEngine);
                    continue;
                } else if (handshakeStatus == HandshakeStatus.FINISHED) {
                    finishHandshake();
                    return null;
                } else if (handshakeStatus == HandshakeStatus.NEED_UNWRAP) {
                    if (src.hasRemaining()) {
                        continue;
                    }
                    return null;
                }
            }
        }
    }

    private ByteBuf wrap(ByteBuf src) throws IOException {
        SSLEngine engine = getSSLEngine();
        ByteBufAllocator alloc = alloc();
        ByteBuf out = null;
        try {
            if (sslHandshakeFinished) {
                byte sslWrapExt = this.sslWrapExt;
                if (sslWrapExt == 0) {
                    out = alloc.allocate(guessWrapOut(src.limit(), 0xff + 1));
                } else {
                    out = alloc.allocate(guessWrapOut(src.limit(), sslWrapExt & 0xff));
                }
                final int SSL_PACKET_BUFFER_SIZE = SslContext.SSL_PACKET_BUFFER_SIZE;
                for (;;) {
                    SSLEngineResult result = engine.wrap(src.nioBuffer(), out.nioBuffer());
                    Status status = result.getStatus();
                    synchByteBuf(result, src, out);
                    if (status == Status.CLOSED) {
                        return out.flip();
                    } else if (status == Status.BUFFER_OVERFLOW) {
                        out.expansion(out.capacity() + SSL_PACKET_BUFFER_SIZE);
                        continue;
                    } else {
                        if (src.hasRemaining()) {
                            continue;
                        }
                        if (sslWrapExt == 0) {
                            int srcLen = src.limit();
                            int outLen = out.position();
                            int y = ((srcLen + 1) / SSL_PACKET_BUFFER_SIZE) + 1;
                            int u = ((outLen - srcLen) / y) * 2;
                            this.sslWrapExt = (byte) u;
                        }
                        return out.flip();
                    }
                }
            } else {
                ByteBuf dst = FastThreadLocal.get().getSslWrapBuf();
                for (;;) {
                    dst.clear();
                    SSLEngineResult result = engine.wrap(src.nioBuffer(), dst.nioBuffer());
                    Status status = result.getStatus();
                    HandshakeStatus handshakeStatus = result.getHandshakeStatus();
                    synchByteBuf(result, src, dst);
                    if (status == Status.CLOSED) {
                        return swap(alloc, dst.flip());
                    }
                    if (handshakeStatus == HandshakeStatus.NEED_UNWRAP) {
                        if (out != null) {
                            out.put(dst.flip());
                            return out.flip();
                        }
                        return swap(alloc, dst.flip());
                    } else if (handshakeStatus == HandshakeStatus.NEED_WRAP) {
                        if (out == null) {
                            out = alloc.allocate(256);
                        }
                        out.put(dst.flip());
                        continue;
                    } else if (handshakeStatus == HandshakeStatus.FINISHED) {
                        finishHandshake();
                        if (out != null) {
                            out.put(dst.flip());
                            return out.flip();
                        }
                        return swap(alloc, dst.flip());
                    } else if (handshakeStatus == HandshakeStatus.NEED_TASK) {
                        runDelegatedTasks(engine);
                        continue;
                    }
                }
            }
        } catch (Throwable e) {
            Util.release(out);
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            throw new IOException(e);
        }
    }

    public void write(ByteBuf buf) {
        if (buf != null) {
            if (enableSsl) {
                ByteBuf old = buf;
                try {
                    buf = wrap(old);
                } catch (Exception e) {
                    printException(logger, e, 1);
                } finally {
                    old.release();
                }
            }
            writeBufs.offer(buf);
            if (!isOpen()) {
                buf.release();
                writeBufs.poll();
            }
        }
    }

    public void write(Frame frame) throws Exception {
        write(codec.encode(this, frame));
    }

    protected int write(NioEventLoopUnsafe unsafe) {
        return this.unsafe.write(unsafe, this);
    }

    public void writeAndFlush(ByteBuf buf) {
        write(buf);
        flush();
    }

    public void writeAndFlush(Frame frame) throws Exception {
        write(codec.encode(this, frame));
        flush();
    }

    abstract static class ChannelUnsafe implements Closeable {

        final Integer channelId;
        final int     localPort;
        final String  remoteAddr;
        final int     remotePort;

        public ChannelUnsafe(String ra, int lp, int rp, Integer id) {
            this.remoteAddr = ra;
            this.localPort = lp;
            this.remotePort = rp;
            this.channelId = id;
        }

        abstract int getOption(int name) throws IOException;

        abstract boolean interestWrite();

        abstract int read(NioEventLoop eventLoop);

        abstract void setOption(int name, int value) throws IOException;

        //1 complete, 0 keep write, -1 close
        abstract int write(NioEventLoopUnsafe unsafe, Channel ch);

    }

    class CloseEvent implements Runnable, Closeable {

        final Channel ch;

        public CloseEvent(Channel ch) {
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

    static final class EpollChannelUnsafe extends ChannelUnsafe {

        private final int epfd;
        private final int fd;
        private boolean   interestWrite;

        public EpollChannelUnsafe(int epfd, int fd, String ra, int lp, int rp) {
            super(ra, lp, rp, fd);
            this.fd = fd;
            this.epfd = epfd;
        }

        @Override
        public void close() {
            int res = Native.epoll_del(this.epfd, this.fd);
            if (res != -1) {
                Native.close(this.fd);
            }
        }

        @Override
        int getOption(int name) throws IOException {
            return Native.get_socket_opt(fd, (name >>> 16), name & 0xff);
        }

        @Override
        boolean interestWrite() {
            return interestWrite;
        }

        @Override
        int read(NioEventLoop eventLoop) {
            ByteBuf buf = eventLoop.getReadBuf();
            return Native.read(fd, eventLoop.getBufAddress() + buf.absPos(), buf.remaining());
        }

        @Override
        void setOption(int name, int value) throws IOException {
            Native.set_socket_opt(fd, (name >>> 16), name & 0xff, value);
        }

        @Override
        int write(NioEventLoopUnsafe unsafe, Channel ch) {
            final int fd = this.fd;
            final ByteBuf[] cw_bufs = ch.currentWriteBufs;
            final Queue<ByteBuf> write_bufs = ch.writeBufs;
            final long iovec = ((EpollNioEventLoopUnsafe) unsafe).getIovec();
            final int iov_len = cw_bufs.length;
            for (;;) {
                int cw_len = ch.currentWriteBufsLen;
                for (; cw_len < iov_len;) {
                    ByteBuf buf = write_bufs.poll();
                    if (buf == null) {
                        break;
                    }
                    cw_bufs[cw_len++] = buf;
                }
                if (cw_len == 0) {
                    interestWrite = false;
                    return 1;
                }
                if (cw_len == 1) {
                    ByteBuf buf = cw_bufs[0];
                    int len = Native.write(fd, buf.address() + buf.absPos(), buf.remaining());
                    if (len == -1) {
                        return -1;
                    }
                    buf.skip(len);
                    if (buf.hasRemaining()) {
                        ch.currentWriteBufsLen = 1;
                        interestWrite = true;
                        return 0;
                    } else {
                        cw_bufs[0] = null;
                        buf.release();
                        ch.currentWriteBufsLen = 0;
                        if (write_bufs.isEmpty()) {
                            interestWrite = false;
                            return 1;
                        }
                        continue;
                    }
                } else {
                    long iov_pos = iovec;
                    for (int i = 0; i < cw_len; i++) {
                        ByteBuf buf = cw_bufs[i];
                        Unsafe.putLong(iov_pos, buf.address() + buf.absPos());
                        iov_pos += 8;
                        Unsafe.putLong(iov_pos, buf.remaining());
                        iov_pos += 8;
                    }
                    long len = Native.writev(fd, iovec, cw_len);
                    if (len == -1) {
                        return -1;
                    }
                    for (int i = 0; i < cw_len; i++) {
                        ByteBuf buf = cw_bufs[i];
                        int r = buf.remaining();
                        if (len < r) {
                            buf.skip((int) len);
                            int remain = cw_len - i;
                            System.arraycopy(cw_bufs, i, cw_bufs, 0, remain);
                            fillNull(cw_bufs, remain, cw_len);
                            interestWrite = true;
                            ch.currentWriteBufsLen = remain;
                            return 0;
                        } else {
                            len -= r;
                            buf.release();
                        }
                    }
                    fillNull(cw_bufs, 0, cw_len);
                    ch.currentWriteBufsLen = 0;
                    if (write_bufs.isEmpty()) {
                        interestWrite = false;
                        return 1;
                    }
                }
            }
        }

    }

    static final class JavaChannelUnsafe extends ChannelUnsafe {

        static final boolean ENABLE_FD;
        static final int     INTEREST_WRITE = INTEREST_WRITE();
        static final Field   S_FD;
        static final Field   S_FD_FD;

        static {
            Field sfd = null;
            Field sfdfd = null;
            boolean enableFd = false;
            try {
                SocketChannel ch = SocketChannel.open();
                sfd = ch.getClass().getDeclaredField("fd");
                if (sfd != null) {
                    Util.trySetAccessible(sfd);
                    Object fo = sfd.get(ch);
                    sfdfd = fo.getClass().getDeclaredField("fd");
                    if (sfdfd != null) {
                        Util.trySetAccessible(sfdfd);
                        Object f2o = sfdfd.get(fo);
                        enableFd = f2o != null;
                    }

                }
            } catch (Throwable e) {}
            S_FD = sfd;
            S_FD_FD = sfdfd;
            ENABLE_FD = enableFd;
        }

        private final SocketChannel channel;
        private boolean             interestWrite;
        private final SelectionKey  key;

        JavaChannelUnsafe(SelectionKey key, String ra, int lp, int rp, Integer chid) {
            super(ra, lp, rp, chid);
            this.key = key;
            this.channel = (SocketChannel) key.channel();
        }

        private void _interestRead() {
            if (interestWrite) {
                interestWrite = false;
                key.interestOps(SelectionKey.OP_READ);
            }
        }

        private void _interestWrite() {
            if (!interestWrite) {
                interestWrite = true;
                key.interestOps(INTEREST_WRITE);
            }
        }

        @Override
        public void close() {
            Util.close(channel);
            key.attach(null);
            key.cancel();
        }

        @Override
        int getOption(int name) throws IOException {
            SocketOption<Object> s = SocketOptions.getSocketOption(name);
            if (s != null) {
                Object res = channel.getOption(s);
                if (res != null) {
                    if (res instanceof Integer) {
                        return (Integer) (res);
                    } else if (res instanceof Boolean) {
                        return ((Boolean) res) ? 1 : 0;
                    }
                }
            }
            return -1;
        }

        @Override
        boolean interestWrite() {
            return interestWrite;
        }

        private int nativeWrite(ByteBuffer src) {
            try {
                return channel.write(src);
            } catch (IOException e) {
                return -1;
            }
        }

        private long nativeWrite(ByteBuffer[] srcs, int off, int len) {
            try {
                return channel.write(srcs, off, len);
            } catch (IOException e) {
                return -1;
            }
        }

        @Override
        int read(NioEventLoop eventLoop) {
            try {
                return channel.read(eventLoop.getReadBuf().nioBuffer());
            } catch (IOException e) {
                return -1;
            }
        }

        @Override
        void setOption(int name, int value) throws IOException {
            SocketOption<Object> s = SocketOptions.getSocketOption(name);
            if (s != null) {
                if (SocketOptions.isParamBoolean(name)) {
                    channel.setOption(s, value != 0);
                } else {
                    channel.setOption(s, value);
                }
            }
        }

        @Override
        int write(NioEventLoopUnsafe unsafe, Channel ch) {
            final ByteBuf[] cwBufs = ch.currentWriteBufs;
            final Queue<ByteBuf> writeBufs = ch.writeBufs;
            final JavaNioEventLoopUnsafe un = (JavaNioEventLoopUnsafe) unsafe;
            final ByteBuffer[] writeBuffers = un.getWriteBuffers();
            final int maxLen = cwBufs.length;
            for (;;) {
                int cwLen = ch.currentWriteBufsLen;
                for (; cwLen < maxLen;) {
                    ByteBuf buf = writeBufs.poll();
                    if (buf == null) {
                        break;
                    }
                    cwBufs[cwLen++] = buf;
                }
                if (cwLen == 0) {
                    _interestRead();
                    return 1;
                }
                if (cwLen == 1) {
                    ByteBuffer nioBuf = cwBufs[0].nioBuffer();
                    int len = nativeWrite(nioBuf);
                    if (len == -1) {
                        return -1;
                    }
                    if (nioBuf.hasRemaining()) {
                        ch.currentWriteBufsLen = 1;
                        cwBufs[0].reverse();
                        _interestWrite();
                        return 0;
                    } else {
                        ByteBuf buf = cwBufs[0];
                        cwBufs[0] = null;
                        buf.release();
                        ch.currentWriteBufsLen = 0;
                        if (writeBufs.isEmpty()) {
                            _interestRead();
                            return 1;
                        }
                        continue;
                    }
                } else {
                    for (int i = 0; i < cwLen; i++) {
                        writeBuffers[i] = cwBufs[i].nioBuffer();
                    }
                    long len = nativeWrite(writeBuffers, 0, cwLen);
                    if (len == -1) {
                        return -1;
                    }
                    for (int i = 0; i < cwLen; i++) {
                        ByteBuf buf = cwBufs[i];
                        if (writeBuffers[i].hasRemaining()) {
                            buf.reverse();
                            int remain = cwLen - i;
                            System.arraycopy(cwBufs, i, cwBufs, 0, remain);
                            fillNull(cwBufs, remain, cwLen);
                            fillNull(writeBuffers, i, cwLen);
                            _interestWrite();
                            ch.currentWriteBufsLen = remain;
                            return 0;
                        } else {
                            writeBuffers[i] = null;
                            buf.release();
                        }
                    }
                    fillNull(cwBufs, 0, cwLen);
                    ch.currentWriteBufsLen = 0;
                    if (writeBufs.isEmpty()) {
                        _interestRead();
                        return 1;
                    }
                }
            }
        }

        static int getFd(SocketChannel javaChannel) {
            try {
                Object fd = S_FD.get(javaChannel);
                Integer fdfd = (Integer) S_FD_FD.get(fd);
                return fdfd.intValue();
            } catch (Throwable e) {
                return -1;
            }
        }

        private static int INTEREST_WRITE() {
            return SelectionKey.OP_READ | SelectionKey.OP_WRITE;
        }

    }

    private static ClosedChannelException CLOSED_WHEN_FLUSH() {
        return Util.unknownStackTrace(new ClosedChannelException(), Channel.class, "flush(...)");
    }

    private static void fillNull(Object[] a, int fromIndex, int toIndex) {
        for (int i = fromIndex; i < toIndex; i++)
            a[i] = null;
    }

    private static Logger newLogger() {
        return LoggerFactory.getLogger(Channel.class);
    }

    private static SSLException NOT_TLS() {
        return Util.unknownStackTrace(new SSLException("NOT TLS"), Channel.class,
                "isEnoughSslUnwrap()");
    }

    private static SSLException SSL_PACKET_OVER_LIMIT() {
        return Util.unknownStackTrace(new SSLException("over limit (" + SSL_PACKET_LIMIT + ")"),
                Channel.class, "isEnoughSslUnwrap()");
    }

    private static SSLException SSL_UNWRAP_OVER_LIMIT() {
        return unknownStackTrace(new SSLException("over limit (SSL_UNWRAP_BUFFER_SIZE)"),
                Channel.class, "unwrap()");
    }

    private static IOException TASK_REJECT() {
        return Util.unknownStackTrace(new IOException(), Channel.class, "accept_reject(...)");
    }

}

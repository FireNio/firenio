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

import static com.firenio.Develop.printException;
import static com.firenio.common.Util.unknownStackTrace;

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

import com.firenio.Develop;
import com.firenio.buffer.ByteBuf;
import com.firenio.buffer.ByteBufAllocator;
import com.firenio.common.Unsafe;
import com.firenio.common.Util;
import com.firenio.concurrent.EventLoop;
import com.firenio.component.NioEventLoop.EpollEventLoop;
import com.firenio.component.NioEventLoop.JavaEventLoop;
import com.firenio.log.Logger;
import com.firenio.log.LoggerFactory;


//请勿使用remote.getRemoteHost(),可能出现阻塞
public abstract class Channel implements Runnable, Closeable {

    public static final ClosedChannelException CLOSED_WHEN_FLUSH     = CLOSED_WHEN_FLUSH();
    public static final InetSocketAddress      ERROR_SOCKET_ADDRESS  = new InetSocketAddress(0);
    public static final Logger                 logger                = NEW_LOGGER();
    public static final SSLException           NOT_TLS               = NOT_TLS();
    public static final int                    SSL_PACKET_LIMIT      = 1024 * 64;
    public static final SSLException           SSL_PACKET_OVER_LIMIT = SSL_PACKET_OVER_LIMIT();
    public static final SSLException           SSL_UNWRAP_OVER_LIMIT = SSL_UNWRAP_OVER_LIMIT();
    public static final IOException            TAST_REJECT           = TASK_REJECT();

    protected final    ChannelContext context;
    protected final    long           creation_time = System.currentTimeMillis();
    protected final    ByteBuf[]      current_wbs;
    protected final    String         desc;
    protected final    boolean        enable_ssl;
    protected final    NioEventLoop   eventLoop;
    protected final    EventLoop      exec_el;
    protected final    SSLEngine      ssl_engine;
    protected final    Queue<ByteBuf> write_bufs;
    protected final    Integer        channelId;
    protected final    int            localPort;
    protected final    String         remoteAddr;
    protected final    int            remotePort;
    protected          Object         attachment;
    protected          ProtocolCodec  codec;
    protected          int            current_wbs_len;
    protected          boolean        in_event;
    protected          long           last_access;
    protected volatile boolean        open          = true;
    protected          ByteBuf        plain_remain_buf;
    protected          boolean        ssl_handshake_finished;
    protected          ByteBuf        ssl_remain_buf;
    protected          byte           ssl_wrap_ext;

    Channel(NioEventLoop el, ChannelContext ctx, String ra, int lp, int rp, Integer id) {
        this.remoteAddr = ra;
        this.localPort = lp;
        this.remotePort = rp;
        this.channelId = id;
        this.context = ctx;
        this.eventLoop = el;
        this.enable_ssl = ctx.isEnableSsl();
        this.codec = ctx.getDefaultCodec();
        this.exec_el = ctx.getNextExecutorEventLoop();
        this.last_access = creation_time + el.getGroup().getIdleTime();
        this.write_bufs = new LinkedBlockingQueue<>();
        this.current_wbs = new ByteBuf[el.getGroup().getWriteBuffers()];
        this.desc = new_desc(Integer.toHexString(channelId));
        if (ctx.isEnableSsl()) {
            this.ssl_engine = ctx.getSslContext().newEngine(getRemoteAddr(), getRemotePort());
        } else {
            this.ssl_handshake_finished = true;
            this.ssl_engine = null;
        }
    }

    private static ClosedChannelException CLOSED_WHEN_FLUSH() {
        return Util.unknownStackTrace(new ClosedChannelException(), Channel.class, "flush(...)");
    }

    static void fill_null(Object[] a, int fromIndex, int toIndex) {
        for (int i = fromIndex; i < toIndex; i++)
            a[i] = null;
    }

    private static Logger NEW_LOGGER() {
        return LoggerFactory.getLogger(Channel.class);
    }

    private static SSLException NOT_TLS() {
        return Util.unknownStackTrace(new SSLException("NOT TLS"), Channel.class, "isEnoughSslUnwrap()");
    }

    private static SSLException SSL_PACKET_OVER_LIMIT() {
        return Util.unknownStackTrace(new SSLException("over limit (" + SSL_PACKET_LIMIT + ")"), Channel.class, "isEnoughSslUnwrap()");
    }

    private static SSLException SSL_UNWRAP_OVER_LIMIT() {
        return unknownStackTrace(new SSLException("over limit (SSL_UNWRAP_BUFFER_SIZE)"), Channel.class, "unwrap()");
    }

    private static IOException TASK_REJECT() {
        return Util.unknownStackTrace(new IOException(), Channel.class, "accept_reject(...)");
    }

    private void accept(ByteBuf src) throws Exception {
        final ProtocolCodec codec      = getCodec();
        final IoEventHandle handle     = getIoEventHandle();
        final boolean       enable_wel = getExecutorEventLoop() != null;
        for (; ; ) {
            Frame f = codec.decode(this, src);
            if (f == null) {
                if (Develop.BUF_DEBUG) {
                    if (plain_remain_buf != null) {
                        throw new Exception("plain_remain_buf not null");
                    }
                }
                plain_remain_buf = slice_remain(src);
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
            exception_caught(f, TAST_REJECT);
        }
    }

    private void accept_line(IoEventHandle handle, Frame frame) {
        try {
            handle.accept(this, frame);
        } catch (Exception e) {
            exception_caught(frame, e);
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
        return alloc().allocate().skip(codec.getHeaderLength());
    }

    public ByteBuf allocate(int limit) {
        int h = codec.getHeaderLength();
        return alloc().allocate(h + limit).skip(h);
    }

    private void check_write_overflow() {
        if (write_bufs.size() > context.getMaxWriteBacklog()) {
            safe_close();
        }
    }

    @Override
    public void close() {
        if (inEventLoop()) {
            safe_close();
        } else {
            if (isOpen()) {
                eventLoop.submit(new Runnable() {

                    @Override
                    public void run() {
                        Channel.this.close();
                    }
                });
            }
        }
    }

    private void close_ssl() {
        if (enable_ssl) {
            ssl_engine.closeOutbound();
            if (context.getSslContext().isClient()) {
                try {
                    write_bufs.offer(wrap(ByteBuf.empty()));
                    write();
                } catch (Exception ignored) {
                }
            }
            try {
                ssl_engine.closeInbound();
            } catch (Exception ignored) {
            }
        }
    }

    public ByteBuf encode(Frame frame) throws Exception {
        return codec.encode(this, frame);
    }

    private void exception_caught(Frame frame, Exception ex) {
        try {
            getIoEventHandle().exceptionCaught(this, frame, ex);
        } catch (Throwable e) {
            printException(logger, e, 2);
            printException(logger, ex, 2);
        }
    }

    private void finish_handshake() {
        this.ssl_handshake_finished = true;
        this.fire_opened();
        this.context.channelEstablish(this, null);
    }

    private void fire_closed() {
        eventLoop.removeChannel(channelId);
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

    protected void fire_opened() {
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
            if (!in_event) {
                in_event = true;
                eventLoop.getJobs().offer(this);
            }
        } else {
            eventLoop.submit(this);
        }
    }

    public Object getAttachment() {
        return attachment;
    }

    public void setAttachment(Object attachment) {
        this.attachment = attachment;
    }

    public Integer getChannelId() {
        return channelId;
    }

    public Charset getCharset() {
        return context.getCharset();
    }

    public ProtocolCodec getCodec() {
        return codec;
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

    public String getCodecId() {
        return codec.getProtocolId();
    }

    public ChannelContext getContext() {
        return context;
    }

    public long getCreationTime() {
        return creation_time;
    }

    public String getDesc() {
        return desc;
    }

    public NioEventLoop getEventLoop() {
        return eventLoop;
    }

    public EventLoop getExecutorEventLoop() {
        return exec_el;
    }

    public IoEventHandle getIoEventHandle() {
        return context.getIoEventHandle();
    }

    public long getLastAccessTime() {
        return last_access;
    }

    public int getLocalPort() {
        return localPort;
    }

    public abstract int getOption(int name) throws IOException;

    public String getRemoteAddr() {
        return remoteAddr;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public SSLEngine getSSLEngine() {
        return ssl_engine;
    }

    public int getWriteBacklog() {
        //忽略current write[]
        return write_bufs.size();
    }

    //FIXME not correct ,fix this
    private int guess_wrap_out(int src, int ext) {
        if (Develop.BUF_DEBUG) {
            return 1;
        } else {
            if (SslContext.OPENSSL_AVAILABLE) {
                return ((src + SslContext.SSL_PACKET_BUFFER_SIZE - 1) / SslContext.SSL_PACKET_BUFFER_SIZE + 1) * ext + src;
            } else {
                return ((src + SslContext.SSL_PACKET_BUFFER_SIZE - 1) / SslContext.SSL_PACKET_BUFFER_SIZE) * (ext + SslContext.SSL_PACKET_BUFFER_SIZE);
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
        return enable_ssl;
    }

    /**
     * <pre>
     * record type (1 byte)
     * /
     * /    version (1 byte major, 1 byte minor)
     * /    /
     * /    /         length (2 bytes)
     * /    /         /
     * +----+----+----+----+----+
     * |    |    |    |    |    |
     * |    |    |    |    |    | TLS Record header
     * +----+----+----+----+----+
     *
     *
     * Record Type Values       dec      hex
     * -------------------------------------
     * CHANGE_CIPHER_SPEC        20     0x14
     * ALERT                     21     0x15
     * HANDSHAKE                 22     0x16
     * APPLICATION_DATA          23     0x17
     *
     *
     * Version Values            dec     hex
     * -------------------------------------
     * SSL 3.0                   3,0  0x0300
     * TLS 1.0                   3,1  0x0301
     * TLS 1.1                   3,2  0x0302
     * TLS 1.2                   3,3  0x0303
     *
     * ref:http://blog.fourthbit.com/2014/12/23/traffic-analysis-of-an-ssl-slash-tls-session/
     * </pre>
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
        return ssl_handshake_finished;
    }

    private String new_desc(String id_hex) {
        StringBuilder sb = FastThreadLocal.get().getStringBuilder();
        sb.append("[id(0x");
        sb.append(id_hex);
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
        last_access = System.currentTimeMillis();
        if (enable_ssl) {
            read_ssl();
        } else {
            read_plain();
        }
    }

    private void read_plain() throws Exception {
        ByteBuf src = eventLoop.getReadBuf();
        for (; ; ) {
            src.clear();
            read_plain_remain(src);
            if (!read_data(src)) {
                return;
            }
            boolean full = src.limit() == src.capacity();
            accept(src);
            if (!full) {
                break;
            }
        }
    }

    private void read_ssl() throws Exception {
        ByteBuf src = eventLoop.getReadBuf();
        for (; ; ) {
            src.clear();
            read_ssl_remain(src);
            if (!read_data(src)) {
                return;
            }
            boolean full = src.limit() == src.capacity();
            for (; ; ) {
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
                        ssl_remain_buf = slice_remain(src);
                    }
                    break;
                }
            }
            if (!full) {
                break;
            }
        }
    }

    private boolean read_data(ByteBuf src) {
        int length = native_read();
        if (length < 1) {
            if (length == -1) {
                Util.close(this);
                return false;
            }
            if (src.position() > 0) {
                src.flip();
                if (enable_ssl) {
                    ssl_remain_buf = slice_remain(src);
                } else {
                    plain_remain_buf = slice_remain(src);
                }
            }
            return false;
        }
        if (Native.EPOLL_AVAIABLE) {
            src.absLimit(src.absPos() + length);
            src.absPos(0);
        } else {
            src.reverse();
            src.flip();
        }
        return true;
    }

    private void read_plain_remain(ByteBuf dst) {
        ByteBuf remainingBuf = this.plain_remain_buf;
        if (remainingBuf == null) {
            return;
        }
        dst.putBytes(remainingBuf);
        remainingBuf.release();
        this.plain_remain_buf = null;
    }

    private void read_ssl_remain(ByteBuf dst) {
        ByteBuf remainingBuf = this.ssl_remain_buf;
        if (remainingBuf == null) {
            return;
        }
        dst.putBytes(remainingBuf);
        remainingBuf.release();
        this.ssl_remain_buf = null;
    }

    public void release(Frame frame) {
        codec.release(eventLoop, frame);
    }

    private void release_wb_array() {
        final ByteBuf[] cwbs   = this.current_wbs;
        final int       maxLen = cwbs.length;
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

    private void release_wb_queue() {
        Queue<ByteBuf> wfs = this.write_bufs;
        if (!wfs.isEmpty()) {
            ByteBuf buf = wfs.poll();
            for (; buf != null; ) {
                Util.release(buf);
                buf = wfs.poll();
            }
        }
    }

    private void remove_channel() {
        Integer id = getChannelId();
        context.getChannelManager().removeChannel(id);
        eventLoop.removeChannel(id);
    }

    @Override
    public void run() {
        if (isOpen()) {
            in_event = false;
            if (interestWrite()) {
                // check write over flow
                check_write_overflow();
            } else {
                if (write() == -1) {
                    safe_close();
                }
            }
        }
    }

    private void run_delegated_tasks(SSLEngine engine) {
        for (; ; ) {
            Runnable task = engine.getDelegatedTask();
            if (task == null) {
                break;
            }
            task.run();
        }
    }

    private void safe_close() {
        if (isOpen()) {
            open = false;
            close_ssl();
            release_wb_array();
            release_wb_queue();
            remove_channel();
            Util.release(ssl_remain_buf);
            Util.release(plain_remain_buf);
            close_channel();
            fire_closed();
            stop_context();
        }
    }

    abstract void close_channel();

    public abstract void setOption(int name, int value) throws IOException;

    private ByteBuf slice_remain(ByteBuf src) {
        int remain = src.remaining();
        if (remain > 0) {
            ByteBuf remaining = alloc().allocate(remain);
            remaining.putBytes(src);
            return remaining.flip();
        } else {
            return null;
        }
    }

    private void stop_context() {
        if (context instanceof ChannelConnector) {
            Util.close(((ChannelConnector) context));
        }
    }

    //FIXME 部分buf不需要swap
    private ByteBuf swap(ByteBufAllocator allocator, ByteBuf buf) {
        ByteBuf out = allocator.allocate(buf.limit());
        out.putBytes(buf);
        return out.flip();
    }

    private void sync_buf(SSLEngineResult result, ByteBuf src, ByteBuf dst) {
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
        ByteBuf   dst       = FastThreadLocal.get().getSslUnwrapBuf();
        if (ssl_handshake_finished) {
            dst.clear();
            read_plain_remain(dst);
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
            sync_buf(result, src, dst);
            return dst.flip();
        } else {
            for (; ; ) {
                dst.clear();
                SSLEngineResult result          = sslEngine.unwrap(src.nioBuffer(), dst.nioBuffer());
                HandshakeStatus handshakeStatus = result.getHandshakeStatus();
                sync_buf(result, src, dst);
                if (handshakeStatus == HandshakeStatus.NEED_WRAP) {
                    writeAndFlush(ByteBuf.empty());
                    return null;
                } else if (handshakeStatus == HandshakeStatus.NEED_TASK) {
                    run_delegated_tasks(sslEngine);
                    continue;
                } else if (handshakeStatus == HandshakeStatus.FINISHED) {
                    finish_handshake();
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
        SSLEngine        engine = getSSLEngine();
        ByteBufAllocator alloc  = alloc();
        ByteBuf          out    = null;
        try {
            if (ssl_handshake_finished) {
                byte sslWrapExt = this.ssl_wrap_ext;
                if (sslWrapExt == 0) {
                    out = alloc.allocate(guess_wrap_out(src.limit(), 0xff + 1));
                } else {
                    out = alloc.allocate(guess_wrap_out(src.limit(), sslWrapExt & 0xff));
                }
                final int SSL_PACKET_BUFFER_SIZE = SslContext.SSL_PACKET_BUFFER_SIZE;
                for (; ; ) {
                    SSLEngineResult result = engine.wrap(src.nioBuffer(), out.nioBuffer());
                    Status          status = result.getStatus();
                    sync_buf(result, src, out);
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
                            int y      = ((srcLen + 1) / SSL_PACKET_BUFFER_SIZE) + 1;
                            int u      = ((outLen - srcLen) / y) * 2;
                            this.ssl_wrap_ext = (byte) u;
                        }
                        return out.flip();
                    }
                }
            } else {
                ByteBuf dst = FastThreadLocal.get().getSslWrapBuf();
                for (; ; ) {
                    dst.clear();
                    SSLEngineResult result          = engine.wrap(src.nioBuffer(), dst.nioBuffer());
                    Status          status          = result.getStatus();
                    HandshakeStatus handshakeStatus = result.getHandshakeStatus();
                    sync_buf(result, src, dst);
                    if (status == Status.CLOSED) {
                        return swap(alloc, dst.flip());
                    }
                    if (handshakeStatus == HandshakeStatus.NEED_UNWRAP) {
                        if (out != null) {
                            out.putBytes(dst.flip());
                            return out.flip();
                        }
                        return swap(alloc, dst.flip());
                    } else if (handshakeStatus == HandshakeStatus.NEED_WRAP) {
                        if (out == null) {
                            out = alloc.allocate(256);
                        }
                        out.putBytes(dst.flip());
                        continue;
                    } else if (handshakeStatus == HandshakeStatus.FINISHED) {
                        finish_handshake();
                        if (out != null) {
                            out.putBytes(dst.flip());
                            return out.flip();
                        }
                        return swap(alloc, dst.flip());
                    } else if (handshakeStatus == HandshakeStatus.NEED_TASK) {
                        run_delegated_tasks(engine);
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
            if (enable_ssl) {
                ByteBuf old = buf;
                try {
                    buf = wrap(old);
                } catch (Exception e) {
                    printException(logger, e, 1);
                } finally {
                    old.release();
                }
            }
            write_bufs.offer(buf);
            if (!isOpen()) {
                buf.release();
                write_bufs.poll();
            }
        }
    }

    public void write(Frame frame) throws Exception {
        write(codec.encode(this, frame));
    }

    //1 complete, 0 keep write, -1 close
    abstract int write();

    public void writeAndFlush(ByteBuf buf) {
        write(buf);
        flush();
    }

    public void writeAndFlush(Frame frame) throws Exception {
        write(codec.encode(this, frame));
        flush();
    }

    abstract boolean interestWrite();

    abstract int native_read();

    static final class EpollChannel extends Channel {

        private final int     epfd;
        private final int     fd;
        private       boolean interestWrite;

        EpollChannel(NioEventLoop el, ChannelContext ctx, int epfd, int fd, String ra, int lp, int rp) {
            super(el, ctx, ra, lp, rp, fd);
            this.fd = fd;
            this.epfd = epfd;
        }

        @Override
        void close_channel() {
            int res = Native.epoll_del(this.epfd, this.fd);
            if (res != -1) {
                Native.close(this.fd);
            }
        }

        @Override
        public int getOption(int name) {
            return Native.get_socket_opt(fd, (name >>> 16), name & 0xff);
        }

        @Override
        boolean interestWrite() {
            return interestWrite;
        }

        @Override
        int native_read() {
            ByteBuf buf = eventLoop.getReadBuf();
            return Native.read(fd, eventLoop.getBufAddress() + buf.absPos(), buf.remaining());
        }

        @Override
        public void setOption(int name, int value) {
            Native.set_socket_opt(fd, (name >>> 16), name & 0xff, value);
        }

        @Override
        int write() {
            final EpollEventLoop el         = (EpollEventLoop) eventLoop;
            final int            fd         = this.fd;
            final ByteBuf[]      cw_bufs    = this.current_wbs;
            final Queue<ByteBuf> write_bufs = this.write_bufs;
            final long           iovec      = el.getIovec();
            final int            iov_len    = cw_bufs.length;
            for (; ; ) {
                int cw_len = this.current_wbs_len;
                for (; cw_len < iov_len; ) {
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
                    int     len = Native.write(fd, buf.address() + buf.absPos(), buf.remaining());
                    if (len == -1) {
                        return -1;
                    }
                    buf.skip(len);
                    if (buf.hasRemaining()) {
                        this.current_wbs_len = 1;
                        interestWrite = true;
                        return 0;
                    } else {
                        cw_bufs[0] = null;
                        buf.release();
                        this.current_wbs_len = 0;
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
                        int     r   = buf.remaining();
                        if (len < r) {
                            buf.skip((int) len);
                            int remain = cw_len - i;
                            System.arraycopy(cw_bufs, i, cw_bufs, 0, remain);
                            fill_null(cw_bufs, remain, cw_len);
                            interestWrite = true;
                            this.current_wbs_len = remain;
                            return 0;
                        } else {
                            len -= r;
                            buf.release();
                        }
                    }
                    fill_null(cw_bufs, 0, cw_len);
                    this.current_wbs_len = 0;
                    if (write_bufs.isEmpty()) {
                        interestWrite = false;
                        return 1;
                    }
                }
            }
        }
    }

    static final class JavaChannel extends Channel {

        static final boolean ENABLE_FD;
        static final int     INTEREST_WRITE = INTEREST_WRITE();
        static final Field   S_FD;
        static final Field   S_FD_FD;

        static {
            Field   sfd      = null;
            Field   sfdfd    = null;
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
            } catch (Throwable ignored) {
            }
            S_FD = sfd;
            S_FD_FD = sfdfd;
            ENABLE_FD = enableFd;
        }

        private final SocketChannel channel;
        private final SelectionKey  key;
        private       boolean       interestWrite;

        JavaChannel(NioEventLoop el, ChannelContext ctx, SelectionKey key, String ra, int lp, int rp, Integer id) {
            super(el, ctx, ra, lp, rp, id);
            this.key = key;
            this.channel = (SocketChannel) key.channel();
        }

        static int getFd(SocketChannel javaChannel) {
            try {
                Object  fd   = S_FD.get(javaChannel);
                Integer fdfd = (Integer) S_FD_FD.get(fd);
                if (fdfd != null) {
                    return fdfd;
                } else {
                    return -1;
                }
            } catch (Throwable e) {
                return -1;
            }
        }

        private static int INTEREST_WRITE() {
            return SelectionKey.OP_READ | SelectionKey.OP_WRITE;
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
        void close_channel() {
            Util.close(channel);
            key.attach(null);
            key.cancel();
        }

        @Override
        public int getOption(int name) throws IOException {
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

        private long nativeWrite(ByteBuffer[] srcs, int len) {
            try {
                return channel.write(srcs, 0, len);
            } catch (IOException e) {
                return -1;
            }
        }

        @Override
        int native_read() {
            try {
                return channel.read(eventLoop.getReadBuf().nioBuffer());
            } catch (IOException e) {
                return -1;
            }
        }

        @Override
        public void setOption(int name, int value) throws IOException {
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
        int write() {
            final ByteBuf[]      cwBufs       = this.current_wbs;
            final Queue<ByteBuf> writeBufs    = this.write_bufs;
            final JavaEventLoop  el           = (JavaEventLoop) eventLoop;
            final ByteBuffer[]   writeBuffers = el.getWriteBuffers();
            final int            maxLen       = cwBufs.length;
            for (; ; ) {
                int cwLen = this.current_wbs_len;
                for (; cwLen < maxLen; ) {
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
                    int        len    = nativeWrite(nioBuf);
                    if (len == -1) {
                        return -1;
                    }
                    if (nioBuf.hasRemaining()) {
                        this.current_wbs_len = 1;
                        cwBufs[0].reverse();
                        _interestWrite();
                        return 0;
                    } else {
                        ByteBuf buf = cwBufs[0];
                        cwBufs[0] = null;
                        buf.release();
                        this.current_wbs_len = 0;
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
                    long len = nativeWrite(writeBuffers, cwLen);
                    if (len == -1) {
                        return -1;
                    }
                    for (int i = 0; i < cwLen; i++) {
                        ByteBuf buf = cwBufs[i];
                        if (writeBuffers[i].hasRemaining()) {
                            buf.reverse();
                            int remain = cwLen - i;
                            System.arraycopy(cwBufs, i, cwBufs, 0, remain);
                            fill_null(cwBufs, remain, cwLen);
                            fill_null(writeBuffers, i, cwLen);
                            _interestWrite();
                            this.current_wbs_len = remain;
                            return 0;
                        } else {
                            writeBuffers[i] = null;
                            buf.release();
                        }
                    }
                    fill_null(cwBufs, 0, cwLen);
                    this.current_wbs_len = 0;
                    if (writeBufs.isEmpty()) {
                        _interestRead();
                        return 1;
                    }
                }
            }
        }

    }

}

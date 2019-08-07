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
import com.firenio.Releasable;
import com.firenio.buffer.ByteBuf;
import com.firenio.buffer.ByteBufAllocator;
import com.firenio.common.Unsafe;
import com.firenio.common.Util;
import com.firenio.component.NioEventLoop.EpollEventLoop;
import com.firenio.component.NioEventLoop.JavaEventLoop;
import com.firenio.concurrent.EventLoop;
import com.firenio.log.Logger;
import com.firenio.log.LoggerFactory;

import static com.firenio.Develop.debugException;
import static com.firenio.common.Util.unknownStackTrace;


//请勿使用remote.getRemoteHost(),可能出现阻塞
public abstract class Channel implements Runnable, Closeable {

    public static final InetSocketAddress ERROR_SOCKET_ADDRESS  = new InetSocketAddress(0);
    public static final Logger            logger                = NEW_LOGGER();
    public static final SSLException      NOT_TLS               = NOT_TLS();
    public static final IOException       CLOSED_CHANNEL        = CLOSED_CHANNEL();
    public static final IOException       SSL_HANDSHAKE_FAILED  = SSL_HANDSHAKE_FAILED();
    public static final int               SSL_PACKET_LIMIT      = 1024 * 64;
    public static final SSLException      SSL_PACKET_OVER_LIMIT = SSL_PACKET_OVER_LIMIT();
    public static final SSLException      SSL_UNWRAP_OVER_LIMIT = SSL_UNWRAP_OVER_LIMIT();
    public static final SSLException      SSL_UNWRAP_EXCEPTION  = SSL_UNWRAP_EXCEPTION();
    public static final IOException       TASK_REJECT           = TASK_REJECT();

    protected final    ChannelContext context;
    protected final    long           creation_time = Util.now();
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
        if (this.enable_ssl) {
            this.ssl_engine = ctx.getSslContext().newEngine(getRemoteAddr(), getRemotePort());
        } else {
            this.ssl_handshake_finished = true;
            this.ssl_engine = null;
        }
    }

    private static ClosedChannelException CLOSED_CHANNEL() {
        return Util.unknownStackTrace(new ClosedChannelException(), Channel.class, "safe_close(...)");
    }

    private static SSLException SSL_HANDSHAKE_FAILED() {
        return Util.unknownStackTrace(new SSLException("ssl handshake failed"), Channel.class, "ssl_handshake(...)");
    }

    private static ClosedChannelException CLOSED_WHEN_FLUSH() {
        return Util.unknownStackTrace(new ClosedChannelException(), Channel.class, "flush(...)");
    }

    private static void fill_null(Object[] a, int fromIndex, int toIndex) {
        for (int i = fromIndex; i < toIndex; i++)
            a[i] = null;
    }

    private static Logger NEW_LOGGER() {
        return LoggerFactory.getLogger(Channel.class);
    }

    private static SSLException NOT_TLS() {
        return Util.unknownStackTrace(new SSLException("not tls"), Channel.class, "isEnoughSslUnwrap()");
    }

    private static SSLException SSL_PACKET_OVER_LIMIT() {
        return Util.unknownStackTrace(new SSLException("over writeIndex (" + SSL_PACKET_LIMIT + ")"), Channel.class, "isEnoughSslUnwrap()");
    }

    private static SSLException SSL_UNWRAP_OVER_LIMIT() {
        return unknownStackTrace(new SSLException("over writeIndex (SSL_UNWRAP_BUFFER_SIZE)"), Channel.class, "unwrap()");
    }

    private static SSLException SSL_UNWRAP_EXCEPTION() {
        return unknownStackTrace(new SSLException("unwrap exception(enable debug to get detail)"), Channel.class, "unwrap()");
    }

    private static IOException TASK_REJECT() {
        return Util.unknownStackTrace(new IOException(), Channel.class, "accept_reject(...)");
    }

    private void slice_remain_plain(ByteBuf src) {
        //ensure the channel is open, otherwise the buf will never be released if the channel closed
        if (isOpen()) {
            plain_remain_buf = slice_remain(src);
        }
    }

    private void slice_remain_ssl(ByteBuf src) {
        if (isOpen()) {
            ssl_remain_buf = slice_remain(src);
        }
    }

    private void accept(ByteBuf src) throws Exception {
        final ProtocolCodec codec      = getCodec();
        final IoEventHandle handle     = getIoEventHandle();
        final EventLoop     eel        = getExecutorEventLoop();
        final boolean       enable_wel = eel != null;
        for (; ; ) {
            Frame f = codec.decode(this, src);
            if (f == null) {
                slice_remain_plain(src);
                break;
            }
            if (enable_wel) {
                accept_async(eel, f);
            } else {
                accept_line(handle, f);
            }
            if (!src.hasReadableBytes()) {
                break;
            }
        }
    }

    private void accept_async(final EventLoop eel, final Frame f) {
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
        if (!eel.submit(job)) {
            exception_caught(f, TASK_REJECT);
        }
    }

    private void accept_line(IoEventHandle handle, Frame frame) {
        try {
            handle.accept(this, frame);
        } catch (Exception e) {
            exception_caught(frame, e);
        }
    }

    public ByteBufAllocator alloc() {
        return eventLoop.alloc();
    }

    public ByteBuf allocate() {
        return allocate(1);
    }

    public ByteBuf allocate(int limit) {
        int h = codec.getHeaderLength();
        return alloc().allocate(h + limit).skipWrite(h);
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
            // fire channelEstablish to tell connector the reason of close
            if (!ssl_handshake_finished) {
                context.channelEstablish(this, SSL_HANDSHAKE_FAILED);
            }
            //ref https://docs.oracle.com/javase/6/docs/technotes/guides/security/jsse/JSSERefGuide.html
            // Indicate that application is done with engine
            if (SslContext.OPENSSL_AVAILABLE && OpenSslHelper.isOpensslEngineDestroyed(ssl_engine)) {
                return;
            }
            ssl_engine.closeOutbound();
            if (!ssl_engine.isOutboundDone()) {
                try {
                    ByteBuf out = wrap(ByteBuf.empty());
                    write_bufs.offer(out);
                    write();
                } catch (Exception e) {
                    debugException(logger, e);
                }
            }
            try {
                if (SslContext.OPENSSL_AVAILABLE) {
                    //set ReceivedShutdown to true to shutdown ssl quiet
                    OpenSslHelper.setOpensslEngineReceivedShutdown(ssl_engine);
                }
                ssl_engine.closeInbound();
            } catch (Exception e) {
                debugException(logger, e);
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
            logger.error(ex.getMessage(), ex);
            logger.error(e.getMessage(), e);
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
            ProtocolCodec codec = context.getProtocolCodec(codecId);
            if (codec == null) {
                throw new IllegalArgumentException("codec not found");
            }
            //FIXME .. is this work?
            synchronized (this) {
                this.codec = codec;
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
            int base = src + SslContext.SSL_PACKET_BUFFER_SIZE - 1;
            if (SslContext.OPENSSL_AVAILABLE) {
                return (base / SslContext.SSL_PACKET_BUFFER_SIZE + 1) * ext + src + 1;
            } else {
                return (base / SslContext.SSL_PACKET_BUFFER_SIZE) * (ext + SslContext.SSL_PACKET_BUFFER_SIZE) + 1;
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
        if (src.readableBytes() < 5) {
            return false;
        }
        int pos = src.readIndex();
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
        if (src.readableBytes() < len) {
            return false;
        }
        if (len > SSL_PACKET_LIMIT) {
            throw SSL_PACKET_OVER_LIMIT;
        }
        src.markWriteIndex();
        src.writeIndex(pos + len);
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
        last_access = Util.now();
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
            accept(src);
            // for epoll et mode
            if (src.hasWritableBytes()) {
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
            for (; ; ) {
                if (isEnoughSslUnwrap(src)) {
                    ByteBuf res = unwrap(src);
                    if (res != null) {
                        accept(res);
                    }
                    src.resetWriteIndex();
                    if (!src.hasReadableBytes()) {
                        break;
                    }
                } else {
                    if (src.hasReadableBytes()) {
                        slice_remain_ssl(src);
                    }
                    break;
                }
            }
            // for epoll et mode
            if (src.hasWritableBytes()) {
                break;
            }
        }
    }

    private boolean read_data(ByteBuf src) {
        int len = native_read();
        if (len < 1) {
            if (len == -1) {
                Util.close(this);
                return false;
            }
            store_remain(src);
            return false;
        }
        src.skipWrite(len);
        return true;
    }

    private void store_remain(ByteBuf src) {
        if (src.hasReadableBytes()) {
            if (enable_ssl) {
                slice_remain_ssl(src);
            } else {
                slice_remain_plain(src);
            }
        }
    }

    private void read_plain_remain(ByteBuf dst) {
        this.read_remain(plain_remain_buf, dst);
        this.plain_remain_buf = null;
    }

    private void read_remain(ByteBuf src, ByteBuf dst) {
        if (src != null) {
            dst.writeBytes(src);
            src.release();
        }
    }

    private void read_ssl_remain(ByteBuf dst) {
        this.read_remain(ssl_remain_buf, dst);
        this.ssl_remain_buf = null;
    }

    public void release(Frame frame) {
        codec.release(eventLoop, frame);
    }

    private static void release(Releasable r) {
        Util.release(r);
    }

    private void release_wb_array() {
        final ByteBuf[] c_wbs   = this.current_wbs;
        final int       max_len = c_wbs.length;
        // 这里有可能是因为异常关闭，currentWriteFrameLen不准确
        // 对所有不为空的frame release
        for (int i = 0; i < max_len; i++) {
            ByteBuf buf = c_wbs[i];
            if (buf == null) {
                break;
            }
            buf.release();
            c_wbs[i] = null;
        }
    }

    private void release_wb_queue() {
        Queue<ByteBuf> wfs = this.write_bufs;
        if (!wfs.isEmpty()) {
            ByteBuf buf = wfs.poll();
            for (; buf != null; ) {
                release(buf);
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
            if (isInterestWrite()) {
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
            release(ssl_remain_buf);
            release(plain_remain_buf);
            close_channel();
            channel_establish();
            fire_closed();
            stop_context();
        }
    }

    private void channel_establish() {
        context.channelEstablish(this, CLOSED_CHANNEL);
    }

    abstract void close_channel();

    public abstract void setOption(int name, int value) throws IOException;

    private ByteBuf slice_remain(ByteBuf src) {
        int remain = src.readableBytes();
        if (remain > 0) {
            ByteBuf remaining = alloc().allocate(remain);
            remaining.writeBytes(src);
            return remaining;
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
        // use writeIndex instead of readableBytes because of the buf readIndex always be zero.
        ByteBuf out = allocator.allocate(buf.writeIndex());
        out.writeBytes(buf);
        return out;
    }

    private void sync_buf(SSLEngineResult result, ByteBuf src, ByteBuf dst) {
        //FIXME 同步。。。。。
        src.reverseRead();
        dst.reverseWrite();
        //      int bytesConsumed = result.bytesConsumed();
        //      int bytesProduced = result.bytesProduced();
        //      if (bytesConsumed > 0) {
        //          src.skipBytes(bytesConsumed);
        //      }
        //      if (bytesProduced > 0) {
        //          dst.skipBytes(bytesProduced);
        //      }
    }

    @Override
    public String toString() {
        return desc;
    }

    private ByteBuf unwrap(ByteBuf src) throws IOException {
        SSLEngine ssl_engine = getSSLEngine();
        ByteBuf   dst        = FastThreadLocal.get().getSslUnwrapBuf();
        if (ssl_handshake_finished) {
            dst.clear();
            read_plain_remain(dst);
            SSLEngineResult result = ssl_engine.unwrap(src.nioReadBuffer(), dst.nioWriteBuffer());
            if (result.getStatus() == Status.BUFFER_OVERFLOW) {
                //why throw an exception here instead of handle it?
                //the getSslUnwrapBuf will return an thread local buffer for unwrap,
                //the buffer's size defined by Constants.SSL_UNWRAP_BUFFER_SIZE_KEY in System property
                //or default value 256KB(1024 * 256), although the buffer will not occupy so much memory because
                //one EventLoop only have one buffer,but before do unwrap, every channel maybe cached a large
                //buffer under SSL_UNWRAP_BUFFER_SIZE,I do not think it is a good way to cached much memory in
                //channel, it is not friendly for load much channels in one system, if you get exception here,
                //you may need find a way to writeIndex you frame size,or cache your incomplete frame's data to
                //file system or others way.
                throw SSL_UNWRAP_OVER_LIMIT;
            }
            sync_buf(result, src, dst);
            return dst;
        } else {
            for (; ; ) {
                dst.clear();
                SSLEngineResult result          = unwrap(ssl_engine, src, dst);
                HandshakeStatus handshakeStatus = result.getHandshakeStatus();
                sync_buf(result, src, dst);
                if (handshakeStatus == HandshakeStatus.NEED_WRAP) {
                    writeAndFlush(ByteBuf.empty());
                    return null;
                } else if (handshakeStatus == HandshakeStatus.NEED_TASK) {
                    run_delegated_tasks(ssl_engine);
                    continue;
                } else if (handshakeStatus == HandshakeStatus.FINISHED) {
                    finish_handshake();
                    return null;
                } else if (handshakeStatus == HandshakeStatus.NEED_UNWRAP) {
                    if (src.hasReadableBytes()) {
                        continue;
                    }
                    return null;
                } else if (result.getStatus() == Status.BUFFER_OVERFLOW) {
                    throw SSL_UNWRAP_OVER_LIMIT;
                }
            }
        }
    }

    private static SSLEngineResult unwrap(SSLEngine ssl_engine, ByteBuf src, ByteBuf dst) throws SSLException {
        try {
            return ssl_engine.unwrap(src.nioReadBuffer(), dst.nioWriteBuffer());
        } catch (SSLException e) {
            logger.error(e.getMessage());
            debugException(logger, e);
        }
        throw SSL_UNWRAP_EXCEPTION;
    }

    private ByteBuf wrap(ByteBuf src) throws IOException {
        SSLEngine        engine = getSSLEngine();
        ByteBufAllocator alloc  = alloc();
        ByteBuf          out    = null;
        try {
            if (ssl_handshake_finished) {
                byte sslWrapExt = this.ssl_wrap_ext;
                if (sslWrapExt == 0) {
                    out = alloc.allocate(guess_wrap_out(src.readableBytes(), 0xff + 1));
                } else {
                    out = alloc.allocate(guess_wrap_out(src.readableBytes(), sslWrapExt & 0xff));
                }
                final int SSL_PACKET_BUFFER_SIZE = SslContext.SSL_PACKET_BUFFER_SIZE;
                for (; ; ) {
                    SSLEngineResult result = engine.wrap(src.nioReadBuffer(), out.nioWriteBuffer());
                    Status          status = result.getStatus();
                    sync_buf(result, src, out);
                    if (status == Status.CLOSED) {
                        return out;
                    } else if (status == Status.BUFFER_OVERFLOW) {
                        out.expansion(out.capacity() + SSL_PACKET_BUFFER_SIZE);
                        continue;
                    } else {
                        if (src.hasReadableBytes()) {
                            continue;
                        }
                        if (sslWrapExt == 0) {
                            int srcLen = src.writeIndex();
                            int outLen = out.readIndex();
                            int y      = ((srcLen + 1) / SSL_PACKET_BUFFER_SIZE) + 1;
                            int u      = ((outLen - srcLen) / y) * 2;
                            this.ssl_wrap_ext = (byte) u;
                        }
                        return out;
                    }
                }
            } else {
                ByteBuf dst = FastThreadLocal.get().getSslWrapBuf();
                for (; ; ) {
                    dst.clear();
                    SSLEngineResult result          = engine.wrap(src.nioReadBuffer(), dst.nioWriteBuffer());
                    Status          status          = result.getStatus();
                    HandshakeStatus handshakeStatus = result.getHandshakeStatus();
                    sync_buf(result, src, dst);
                    if (status == Status.CLOSED) {
                        return swap(alloc, dst);
                    }
                    if (handshakeStatus == HandshakeStatus.NEED_UNWRAP) {
                        if (out != null) {
                            out.writeBytes(dst);
                            return out;
                        }
                        return swap(alloc, dst);
                    } else if (handshakeStatus == HandshakeStatus.NEED_WRAP) {
                        if (out == null) {
                            out = alloc.allocate(256);
                        }
                        out.writeBytes(dst);
                        continue;
                    } else if (handshakeStatus == HandshakeStatus.FINISHED) {
                        finish_handshake();
                        if (out != null) {
                            out.writeBytes(dst);
                            return out;
                        }
                        return swap(alloc, dst);
                    } else if (handshakeStatus == HandshakeStatus.NEED_TASK) {
                        run_delegated_tasks(engine);
                        continue;
                    }
                }
            }
        } catch (Throwable e) {
            release(out);
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
                    debugException(logger, e);
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

    abstract boolean isInterestWrite();

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
        boolean isInterestWrite() {
            return interestWrite;
        }

        @Override
        int native_read() {
            ByteBuf buf = eventLoop.getReadBuf();
            return Native.read(fd, eventLoop.getBufAddress() + buf.absWriteIndex(), buf.writableBytes());
        }

        @Override
        public void setOption(int name, int value) {
            Native.set_socket_opt(fd, (name >>> 16), name & 0xff, value);
        }

        @Override
        int write() {
            final EpollEventLoop el        = (EpollEventLoop) eventLoop;
            final int            fd        = this.fd;
            final ByteBuf[]      cwb_array = this.current_wbs;
            final Queue<ByteBuf> wb_queue  = this.write_bufs;
            final long           iovec     = el.getIovec();
            final int            iov_len   = cwb_array.length;
            for (; ; ) {
                int cw_len = this.current_wbs_len;
                for (; cw_len < iov_len; ) {
                    ByteBuf buf = wb_queue.poll();
                    if (buf == null) {
                        break;
                    }
                    cwb_array[cw_len++] = buf;
                }
                if (cw_len == 0) {
                    interestWrite = false;
                    return 1;
                }
                if (cw_len == 1) {
                    ByteBuf buf     = cwb_array[0];
                    long    address = buf.address() + buf.absReadIndex();
                    int     len     = Native.write(fd, address, buf.readableBytes());
                    if (len == -1) {
                        return -1;
                    }
                    buf.skipRead(len);
                    if (buf.hasReadableBytes()) {
                        this.current_wbs_len = 1;
                        this.interestWrite = true;
                        return 0;
                    } else {
                        buf.release();
                        this.current_wbs_len = 0;
                        if (wb_queue.isEmpty()) {
                            cwb_array[0] = null;
                            this.interestWrite = false;
                            return 1;
                        }
                        continue;
                    }
                } else {
                    long iov_pos = iovec;
                    for (int i = 0; i < cw_len; i++) {
                        ByteBuf buf = cwb_array[i];
                        Unsafe.putLong(iov_pos, buf.address() + buf.absReadIndex());
                        iov_pos += 8;
                        Unsafe.putLong(iov_pos, buf.readableBytes());
                        iov_pos += 8;
                    }
                    long len = Native.writev(fd, iovec, cw_len);
                    if (len == -1) {
                        return -1;
                    }
                    for (int i = 0; i < cw_len; i++) {
                        ByteBuf buf = cwb_array[i];
                        int     r   = buf.readableBytes();
                        if (len < r) {
                            buf.skipRead((int) len);
                            int remain = cw_len - i;
                            System.arraycopy(cwb_array, i, cwb_array, 0, remain);
                            fill_null(cwb_array, remain, cw_len);
                            this.interestWrite = true;
                            this.current_wbs_len = remain;
                            return 0;
                        } else {
                            len -= r;
                            buf.release();
                        }
                    }
                    fill_null(cwb_array, 0, cw_len);
                    this.current_wbs_len = 0;
                    if (wb_queue.isEmpty()) {
                        this.interestWrite = false;
                        return 1;
                    }
                }
            }
        }
    }

    static final class JavaChannel extends Channel {

        static final int INTEREST_WRITE = INTEREST_WRITE();

        private final SocketChannel channel;
        private final SelectionKey  key;
        private       boolean       interestWrite;

        JavaChannel(NioEventLoop el, ChannelContext ctx, SelectionKey key, String ra, int lp, int rp, Integer id) {
            super(el, ctx, ra, lp, rp, id);
            this.key = key;
            this.channel = (SocketChannel) key.channel();
        }

        private static int INTEREST_WRITE() {
            return SelectionKey.OP_READ | SelectionKey.OP_WRITE;
        }

        private void interestRead() {
            if (interestWrite) {
                interestWrite = false;
                key.interestOps(SelectionKey.OP_READ);
            }
        }

        private void interestWrite() {
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
        boolean isInterestWrite() {
            return interestWrite;
        }

        private int native_write(ByteBuffer src) {
            try {
                return channel.write(src);
            } catch (IOException e) {
                return -1;
            }
        }

        private long native_write(ByteBuffer[] srcs, int len) {
            try {
                return channel.write(srcs, 0, len);
            } catch (IOException e) {
                return -1;
            }
        }

        @Override
        int native_read() {
            try {
                return channel.read(eventLoop.getReadBuf().nioWriteBuffer());
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
            final ByteBuf[]      cwb_array   = this.current_wbs;
            final Queue<ByteBuf> wb_queue    = this.write_bufs;
            final JavaEventLoop  el          = (JavaEventLoop) eventLoop;
            final ByteBuffer[]   wb_array    = el.getWriteBuffers();
            final int            max_cwb_len = cwb_array.length;
            for (; ; ) {
                int cwb_len = this.current_wbs_len;
                for (; cwb_len < max_cwb_len; ) {
                    ByteBuf buf = wb_queue.poll();
                    if (buf == null) {
                        break;
                    }
                    cwb_array[cwb_len++] = buf;
                }
                if (cwb_len == 0) {
                    interestRead();
                    return 1;
                }
                if (cwb_len == 1) {
                    ByteBuf    buf    = cwb_array[0];
                    ByteBuffer nioBuf = buf.nioReadBuffer();
                    int        len    = native_write(nioBuf);
                    if (len == -1) {
                        return -1;
                    }
                    if (nioBuf.hasRemaining()) {
                        this.current_wbs_len = 1;
                        buf.reverseRead();
                        interestWrite();
                        return 0;
                    } else {
                        buf.release();
                        this.current_wbs_len = 0;
                        if (wb_queue.isEmpty()) {
                            cwb_array[0] = null;
                            interestRead();
                            return 1;
                        }
                    }
                } else {
                    for (int i = 0; i < cwb_len; i++) {
                        wb_array[i] = cwb_array[i].nioReadBuffer();
                    }
                    long len = native_write(wb_array, cwb_len);
                    if (len == -1) {
                        return -1;
                    }
                    for (int i = 0; i < cwb_len; i++) {
                        ByteBuf buf = cwb_array[i];
                        if (wb_array[i].hasRemaining()) {
                            buf.reverseRead();
                            int remain = cwb_len - i;
                            System.arraycopy(cwb_array, i, cwb_array, 0, remain);
                            fill_null(cwb_array, remain, cwb_len);
                            fill_null(wb_array, i, cwb_len);
                            interestWrite();
                            this.current_wbs_len = remain;
                            return 0;
                        } else {
                            wb_array[i] = null;
                            buf.release();
                        }
                    }
                    fill_null(cwb_array, 0, cwb_len);
                    this.current_wbs_len = 0;
                    if (wb_queue.isEmpty()) {
                        interestRead();
                        return 1;
                    }
                }
            }
        }

    }

    static class OpenSslHelper {

        private static final Field OPENSSL_RECEIVED_SHUTDOWN = OPENSSL_RECEIVED_SHUTDOWN();
        private static final Field OPENSSL_DESTROYED         = OPENSSL_DESTROYED();

        private static Field OPENSSL_RECEIVED_SHUTDOWN() {
            if (SslContext.OPENSSL_AVAILABLE) {
                try {
                    Field f = org.wildfly.openssl.OpenSSLEngine.class.getDeclaredField("receivedShutdown");
                    f.setAccessible(true);
                    return f;
                } catch (NoSuchFieldException e) {
                    throw new Error(e);
                }
            } else {
                return null;
            }
        }

        private static Field OPENSSL_DESTROYED() {
            if (SslContext.OPENSSL_AVAILABLE) {
                try {
                    Field f = org.wildfly.openssl.OpenSSLEngine.class.getDeclaredField("destroyed");
                    f.setAccessible(true);
                    return f;
                } catch (NoSuchFieldException e) {
                    throw new Error(e);
                }
            } else {
                return null;
            }
        }

        static boolean isOpensslEngineDestroyed(SSLEngine sslEngine) {
            try {
                return ((Integer) OPENSSL_DESTROYED.get(sslEngine)) == 1;
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        static void setOpensslEngineReceivedShutdown(SSLEngine sslEngine) {
            try {
                OPENSSL_RECEIVED_SHUTDOWN.set(sslEngine, true);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

    }

}

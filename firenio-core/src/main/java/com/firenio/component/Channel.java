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

import com.firenio.Develop;
import com.firenio.Releasable;
import com.firenio.buffer.ByteBuf;
import com.firenio.buffer.ByteBufAllocator;
import com.firenio.common.Unsafe;
import com.firenio.common.Util;
import com.firenio.concurrent.EventLoop;
import com.firenio.log.Logger;
import com.firenio.log.LoggerFactory;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;
import javax.net.ssl.SSLException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

import static com.firenio.Develop.debugException;
import static com.firenio.common.Util.unknownStackTrace;

/**
 * Channel is a nexus to transport data between endpoints, such as read, write, close.<br>
 * You will never driving read operation in your owner thread,
 * the read method is used to read data and then call decode in io thread(NioEventLoop).<br>
 * You can call write(ByteBuf/Frame) in any thread, <b>and don't forget to flush</b>,
 * otherwise you can call writeAndFlush instead of write(...) ... flush.
 */
//请勿使用remote.getRemoteHost(),可能出现阻塞
public abstract class Channel implements Runnable, AutoCloseable {

    static final Logger                          logger                     = NEW_LOGGER();
    static final long                            PENDING_WRITE_SIZE_OFFSET  = Unsafe.fieldOffset(Channel.class, "pending_write_size");
    static final AtomicLongFieldUpdater<Channel> PENDING_WRITE_SIZE_UPDATER = PENDING_WRITE_SIZE_UPDATER();

    public static final IOException  CLOSED_CHANNEL       = CLOSED_CHANNEL();
    public static final IOException  SSL_HANDSHAKE_FAILED = SSL_HANDSHAKE_FAILED();
    public static final SSLException SSL_WRAP_CLOSED      = SSL_WRAP_CLOSED();

    protected final    ChannelContext      context;
    protected final    long                creation_time = Util.now();
    protected final    ByteBuf[]           current_wb_array;
    protected final    String              desc;
    protected final    boolean             enable_ssl;
    protected final    NioEventLoop        eventLoop;
    protected final    EventLoop           exec_el;
    protected final    SSLEngine           ssl_engine;
    protected final    Queue<ByteBuf>      write_buf_queue;
    protected final    IoEventHandle       ioEventHandle;
    protected final    int                 channelId;
    protected final    short               localPort;
    protected final    String              remoteAddress;
    protected final    short               remotePort;
    protected final    long                max_pending_write_size;
    protected          Object              attachment;
    protected          Map<String, Object> attributes;
    protected          ProtocolCodec       codec;
    protected          byte                current_wb_len;
    protected          boolean             in_event;
    protected          boolean             interestWrite;
    protected          long                last_access;
    protected          ByteBuf             plain_remain_buf;
    protected          boolean             ssl_handshake_finished;
    protected          ByteBuf             ssl_remain_buf;
    protected          byte                ssl_wrap_ext;
    protected volatile boolean             open          = true;
    protected volatile long                pending_write_size;

    Channel(NioEventLoop el, ChannelContext ctx, String ra, int lp, int rp, Integer id) {
        this.remoteAddress = ra;
        this.localPort = (short) lp;
        this.remotePort = (short) rp;
        this.channelId = id;
        this.context = ctx;
        this.eventLoop = el;
        this.enable_ssl = ctx.isEnableSsl();
        this.codec = ctx.getDefaultCodec();
        this.exec_el = ctx.getNextExecutorEventLoop();
        this.write_buf_queue = new ConcurrentLinkedQueue<>();
        this.ioEventHandle = context.getIoEventHandle();
        this.last_access = creation_time + el.getGroup().getIdleTime();
        this.current_wb_array = new ByteBuf[el.getGroup().getWriteBuffers()];
        this.max_pending_write_size = context.getMaxPendingWriteSize();
        this.desc = new_desc(Integer.toHexString(channelId));
        if (this.enable_ssl) {
            this.ssl_engine = ctx.getSslContext().newEngine(getRemoteAddress(), getRemotePort());
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

    private static void fill_null(Object[] a, int fromIndex, int toIndex) {
        for (int i = fromIndex; i < toIndex; i++)
            a[i] = null;
    }

    private static Logger NEW_LOGGER() {
        return LoggerFactory.getLogger(Channel.class);
    }

    private static SSLException SSL_WRAP_CLOSED() {
        return unknownStackTrace(new SSLException("SSL_WRAP_CLOSED"), Channel.class, "wrap()");
    }

    private static AtomicLongFieldUpdater<Channel> PENDING_WRITE_SIZE_UPDATER() {
        return AtomicLongFieldUpdater.newUpdater(Channel.class, "pending_write_size");
    }

    /**
     * Return the allocator in event loop that the channel belong to.
     */
    public ByteBufAllocator alloc() {
        return eventLoop.alloc();
    }

    /**
     * Allocate a ByteBuf with n(a memory unit) byte
     */
    public ByteBuf allocate() {
        return allocate(1);
    }

    /**
     * Allocate a ByteBuf with n((n+unit-1)/unit*unit) byte
     */
    public ByteBuf allocate(int limit) {
        return alloc().allocate(limit);
    }

    public ByteBuf allocateWithSkipHeader(int limit) {
        int h = codec.getHeaderLength();
        return allocate(h + limit).skipWrite(h);
    }

    /**
     * Close the channel.
     */
    @Override
    public void close() {
        if (inEventLoop()) {
            safe_close();
        } else {
            if (isOpen()) {
                eventLoop.submit(Channel.this::close);
            }
        }
    }

    private void close_ssl() {
        if (enable_ssl) {
            close_ssl0();
        }
    }

    private void close_ssl0() {
        // Fire channelEstablish to tell connector the reason of close.
        if (!ssl_handshake_finished) {
            context.channelEstablish(this, SSL_HANDSHAKE_FAILED);
        }
        //Ref https://docs.oracle.com/javase/6/docs/technotes/guides/security/jsse/JSSERefGuide.html
        // Indicate that application is done with engine
        if (SslContext.OPENSSL_AVAILABLE && OpenSslHelper.isOpensslEngineDestroyed(ssl_engine)) {
            return;
        }
        ssl_engine.closeOutbound();
        if (!ssl_engine.isOutboundDone()) {
            try {
                ByteBuf out = wrap(ByteBuf.empty());
                write_buf_queue.offer(out);
                write();
            } catch (Throwable e) {
                debugException(logger, e);
            }
        }
        try {
            if (SslContext.OPENSSL_AVAILABLE) {
                //Set ReceivedShutdown to true to shutdown ssl quiet.
                OpenSslHelper.setOpensslEngineReceivedShutdown(ssl_engine);
            }
            ssl_engine.closeInbound();
        } catch (Throwable e) {
            debugException(logger, e);
        }
    }

    /**
     * Encode the frame to a ByteBuf, it may be return null in some protocol if
     * there is nothing to encode or ByteBuf is already write to channel and no need to write any more.
     *
     * @param frame
     * @return
     * @throws Exception
     */
    public ByteBuf encode(Frame frame) throws Exception {
        return codec.encode(this, frame);
    }

    protected void finish_handshake() {
        this.ssl_handshake_finished = true;
        this.fire_opened();
        this.context.channelEstablish(this, null);
    }

    private void fire_closed() {
        List<ChannelEventListener> ls = context.getChannelEventListeners();
        for (int i = 0, count = ls.size(); i < count; i++) {
            ChannelEventListener l = ls.get(i);
            try {
                l.channelClosed(this);
            } catch (Throwable e) {
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
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
                Util.close(this);
                return;
            }
            if (!this.isOpen()) {
                return;
            }
        }
    }

    /**
     * Flush ByteBufs to event loop, if the current thread equals the target io thread,
     * the ByteBufs will be write after io events handled.
     */
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

    // Why the attachment is not volatile ? see setCodec.
    public void setAttachment(Object attachment) {
        this.attachment = attachment;
    }

    protected abstract int getFd();

    public int getChannelId() {
        return channelId;
    }

    public Charset getCharset() {
        return context.getCharset();
    }

    public ProtocolCodec getCodec() {
        return codec;
    }

    // Why the codec is not volatile ?
    // This method always used by protocol update, and that thread deserved see the newest value of codec,
    // the io thread will see the newest value after select(wakeup) too.
    public void setCodec(String codecId) throws IOException {
        if (inEventLoop()) {
            this.codec = context.getProtocolCodec(codecId);
        } else {
            ProtocolCodec codec = context.getProtocolCodec(codecId);
            if (codec == null) {
                throw new IllegalArgumentException("codec not found");
            }
            this.codec = codec;
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
        return ioEventHandle;
    }

    public long getLastAccessTime() {
        return last_access;
    }

    public int getLocalPort() {
        return localPort & 0xffff;
    }

    public abstract int getOption(int name) throws IOException;

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public int getRemotePort() {
        return remotePort & 0xffff;
    }

    public SSLEngine getSSLEngine() {
        return ssl_engine;
    }

    /**
     * @return The pending write bytes of channel
     */
    public long getPendingWriteSize() {
        return pending_write_size;
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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Channel) {
            return ((Channel) obj).desc.equals(desc);
        }
        return false;
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
        sb.append(getRemoteAddress());
        sb.append(':');
        sb.append(getRemotePort());
        sb.append("; L:");
        sb.append(getLocalPort());
        sb.append("]");
        return sb.toString();
    }

    /**
     * Not safe API, DO NOT USE this unless you are really know how to use this.
     */
    public void read() throws Exception {
        last_access = Util.now();
        if (enable_ssl) {
            codec.read_ssl(this);
        } else {
            codec.read_plain(this);
        }
    }

    protected void read_plain_remain(ByteBuf dst) {
        ByteBuf remain = this.plain_remain_buf;
        if (remain != null) {
            dst.writeBytes(remain);
            remain.release();
            this.plain_remain_buf = null;
        }
    }

    protected void read_ssl_remain(ByteBuf dst) {
        ByteBuf remain = this.ssl_remain_buf;
        if (remain != null) {
            dst.writeBytes(remain);
            remain.release();
            this.ssl_remain_buf = null;
        }
    }

    protected void store_plain_remain(ByteBuf src) {
        //ensure the channel is open, otherwise the buf will never be released if the channel closed
        if (src.hasReadableBytes() && isOpen()) {
            plain_remain_buf = slice_remain(src);
        }
    }

    protected void store_ssl_remain(ByteBuf src) {
        //ensure the channel is open, otherwise the buf will never be released if the channel closed
        if (src.hasReadableBytes() && isOpen()) {
            ssl_remain_buf = slice_remain(src);
        }
    }

    /**
     * Release the frame to event loop store if there is an store in event loop.
     */
    public void release(Frame frame) {
        codec.release(eventLoop, frame);
    }

    private static void release(Releasable r) {
        Util.release(r);
    }

    private void release_wb_array() {
        final ByteBuf[] c_wbs   = this.current_wb_array;
        final int       max_len = c_wbs.length;
        // 这里有可能是因为异常关闭，current_wbs_len不准确
        // 对所有不为空的ByteBuf release
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
        Queue<ByteBuf> wfs = this.write_buf_queue;
        ByteBuf        buf = wfs.poll();
        for (; buf != null; ) {
            release(buf);
            buf = wfs.poll();
        }
    }

    private void remove_channel() {
        eventLoop.removeChannel(getFd());
    }

    /**
     * Please do not call this method, this is not for user,
     * it is used by run write event or something else.
     */
    @Override
    public void run() {
        if (isOpen()) {
            in_event = false;
            if (!interestWrite && write() == -1) {
                safe_close();
            }
        }
    }

    protected void run_delegated_tasks(SSLEngine engine) {
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

    /**
     * Set socket options for channel, the keys in {@link SocketOptions}.
     */
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

    protected void sync_buf(ByteBuf src, ByteBuf dst) {
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
                    sync_buf(src, out);
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
                    sync_buf(src, dst);
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
                    } else if (handshakeStatus == HandshakeStatus.NOT_HANDSHAKING) {
                        // NOTICE: this handle may not the NOT_HANDSHAKING expected
                        // It is shouldn't here to have "NOT_HANDSHAKING", because of the ssl is closed?
                        throw SSL_WRAP_CLOSED;
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

    /**
     * Write a ByteBuf to channel, this method will not tell you write result,
     * the buf will auto released if write failed.
     */
    public void write(ByteBuf buf) {
        if (buf != null) {
            if (enable_ssl) {
                ByteBuf old = buf;
                try {
                    buf = wrap(old);
                } catch (Throwable e) {
                    debugException(logger, e);
                } finally {
                    old.release();
                }
            }
            int write_size = buf.readableBytes();
            if (isPendingOverflow(write_size)) {
                buf.release();
                close();
                return;
            }
            Queue<ByteBuf> write_buf_queue = this.write_buf_queue;
            write_buf_queue.offer(buf);
            if (!isOpen() && write_buf_queue.remove(buf)) {
                buf.release();
            }
        }
    }

    private boolean isPendingOverflow(int size) {
        return inc_pending_write_size(size) > max_pending_write_size;
    }

    /**
     * Encode the frame to buf and write buf to channel.
     */
    public void write(Frame frame) throws Exception {
        write(codec.encode(this, frame));
    }

    /**
     * Not safe API, DO NOT USE this unless you are really know how to use this.
     *
     * @return 1 complete, 0 keep write, -1 close
     */
    public abstract int write();

    public <T> T getAttribute(String key) {
        if (attributes == null) {
            return null;
        }
        return (T) attributes.get(key);
    }

    public <T> T setAttribute(String key, T value) {
        if (attributes == null) {
            synchronized (this) {
                //isOpen is used to set memory barrier
                isOpen();
                if (attributes == null) {
                    attributes = new ConcurrentHashMap<>();
                }
            }
        }
        return (T) attributes.put(key, value);
    }

    public Set<String> getAttributeKeys() {
        if (attributes == null) {
            return null;
        }
        return attributes.keySet();
    }

    public void writeAndFlush(ByteBuf buf) {
        write(buf);
        flush();
    }

    public void writeAndFlush(Frame frame) throws Exception {
        write(codec.encode(this, frame));
        flush();
    }

    long inc_pending_write_size(long size) {
        if (Unsafe.UNSAFE_AVAILABLE) {
            return Unsafe.addAndGetLong(this, PENDING_WRITE_SIZE_OFFSET, size);
        } else {
            return PENDING_WRITE_SIZE_UPDATER.addAndGet(this, size);
        }
    }

    /**
     * Not safe API, DO NOT USE this unless you are really know how to use this.
     *
     * @return read len, -1 EOF
     */
    public abstract int read(ByteBuf dst);

    static final class EpollChannel extends Channel {

        private final int epfd;
        private final int fd;

        EpollChannel(NioEventLoop el, ChannelContext ctx, int epfd, int fd, String ra, int lp, int rp, Integer id) {
            super(el, ctx, ra, lp, rp, id);
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
        protected int getFd() {
            return fd;
        }

        @Override
        public int getOption(int name) {
            return Native.get_socket_opt(fd, (name >>> 16), name & 0xff);
        }

        @Override
        public int read(ByteBuf dst) {
            int writableBytes = dst.writableBytes();
            if (writableBytes == 0) {
                return 0;
            }
            long address = dst.address() + dst.absWriteIndex();
            return Native.read(fd, address, writableBytes);
        }

        @Override
        public void setOption(int name, int value) {
            Native.set_socket_opt(fd, (name >>> 16), name & 0xff, value);
        }

        @Override
        public int write() {
            final EpollEventLoop el        = (EpollEventLoop) eventLoop;
            final int            fd        = this.fd;
            final ByteBuf[]      cwb_array = this.current_wb_array;
            final Queue<ByteBuf> wb_queue  = this.write_buf_queue;
            final long           iovec     = el.getIovec();
            final int            iov_len   = cwb_array.length;
            for (; ; ) {
                int cw_len = this.current_wb_len;
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
                    inc_pending_write_size(-len);
                    buf.skipRead(len);
                    if (buf.hasReadableBytes()) {
                        this.current_wb_len = 1;
                        this.interestWrite = true;
                        return 0;
                    } else {
                        buf.release();
                        this.current_wb_len = 0;
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
                    //TODO
                    inc_pending_write_size(-len);
                    for (int i = 0; i < cw_len; i++) {
                        ByteBuf buf = cwb_array[i];
                        int     r   = buf.readableBytes();
                        if (len < r) {
                            buf.skipRead((int) len);
                            int remain = cw_len - i;
                            System.arraycopy(cwb_array, i, cwb_array, 0, remain);
                            fill_null(cwb_array, remain, cw_len);
                            this.interestWrite = true;
                            this.current_wb_len = (byte) remain;
                            return 0;
                        } else {
                            len -= r;
                            buf.release();
                        }
                    }
                    fill_null(cwb_array, 0, cw_len);
                    this.current_wb_len = 0;
                    if (wb_queue.isEmpty()) {
                        this.interestWrite = false;
                        return 1;
                    }
                }
            }
        }
    }

    static final class JavaChannel extends Channel {

        static final int INTEREST_WRITE = SelectionKey.OP_READ | SelectionKey.OP_WRITE;

        private final SocketChannel channel;
        private final SelectionKey  key;

        JavaChannel(NioEventLoop el, ChannelContext ctx, SelectionKey key, String ra, int lp, int rp, Integer id) {
            super(el, ctx, ra, lp, rp, id);
            this.key = key;
            this.channel = (SocketChannel) key.channel();
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

        // not really fd
        @Override
        protected int getFd() {
            return getChannelId();
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

        private int native_write(ByteBuffer src) {
            try {
                return channel.write(src);
            } catch (Throwable e) {
                return -1;
            }
        }

        private long native_write(ByteBuffer[] srcs, int len) {
            try {
                return channel.write(srcs, 0, len);
            } catch (Throwable e) {
                return -1;
            }
        }

        @Override
        public int read(ByteBuf dst) {
            int writableBytes = dst.writableBytes();
            if (writableBytes == 0) {
                return 0;
            }
            try {
                return channel.read(dst.nioWriteBuffer());
            } catch (Throwable e) {
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
        public int write() {
            final ByteBuf[]      cwb_array   = this.current_wb_array;
            final Queue<ByteBuf> wb_queue    = this.write_buf_queue;
            final JavaEventLoop  el          = (JavaEventLoop) eventLoop;
            final ByteBuffer[]   wb_array    = el.getWriteBuffers();
            final int            max_cwb_len = cwb_array.length;
            for (; ; ) {
                int cwb_len = this.current_wb_len;
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
                    inc_pending_write_size(-len);
                    if (nioBuf.hasRemaining()) {
                        this.current_wb_len = 1;
                        buf.reverseRead();
                        interestWrite();
                        return 0;
                    } else {
                        buf.release();
                        this.current_wb_len = 0;
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
                    inc_pending_write_size(-len);
                    for (int i = 0; i < cwb_len; i++) {
                        ByteBuf buf = cwb_array[i];
                        if (wb_array[i].hasRemaining()) {
                            buf.reverseRead();
                            int remain = cwb_len - i;
                            System.arraycopy(cwb_array, i, cwb_array, 0, remain);
                            fill_null(cwb_array, remain, cwb_len);
                            fill_null(wb_array, i, cwb_len);
                            interestWrite();
                            this.current_wb_len = (byte) remain;
                            return 0;
                        } else {
                            wb_array[i] = null;
                            buf.release();
                        }
                    }
                    fill_null(cwb_array, 0, cwb_len);
                    this.current_wb_len = 0;
                    if (wb_queue.isEmpty()) {
                        interestRead();
                        return 1;
                    }
                }
            }
        }

    }

    static class OpenSslHelper {

        private static final Class   OPENSSL_CLASS                       = OPENSSL_CLASS();
        private static final Field   OPENSSL_RECEIVED_SHUTDOWN           = OPENSSL_RECEIVED_SHUTDOWN();
        private static final Field   OPENSSL_DESTROYED                   = OPENSSL_DESTROYED();
        private static final Field   OPENSSL_MAX_ENCRYPTED_PACKET_LENGTH = OPENSSL_MAX_ENCRYPTED_PACKET_LENGTH();
        private static final long    OPENSSL_DESTROYED_OFFSET            = OPENSSL_DESTROYED_OFFSET();
        private static final long    OPENSSL_RECEIVED_SHUTDOWN_OFFSET    = OPENSSL_RECEIVED_SHUTDOWN_OFFSET();
        private static final boolean ENABLE_OPENSSL_UNSAFE               = true;

        private static Field OPENSSL_RECEIVED_SHUTDOWN() {
            if (SslContext.OPENSSL_AVAILABLE) {
                try {
                    Field f = OPENSSL_CLASS.getDeclaredField("receivedShutdown");
                    f.setAccessible(true);
                    return f;
                } catch (NoSuchFieldException e) {
                    throw new Error(e);
                }
            } else {
                return null;
            }
        }

        private static Class OPENSSL_CLASS() {
            if (SslContext.OPENSSL_AVAILABLE) {
                return org.wildfly.openssl.OpenSSLEngine.class;
            } else {
                return null;
            }
        }

        private static Field OPENSSL_DESTROYED() {
            if (SslContext.OPENSSL_AVAILABLE) {
                try {
                    Field f = OPENSSL_CLASS.getDeclaredField("destroyed");
                    f.setAccessible(true);
                    return f;
                } catch (NoSuchFieldException e) {
                    throw new Error(e);
                }
            } else {
                return null;
            }
        }

        private static Field OPENSSL_MAX_ENCRYPTED_PACKET_LENGTH() {
            if (SslContext.OPENSSL_AVAILABLE) {
                try {
                    Field f = OPENSSL_CLASS.getDeclaredField("MAX_ENCRYPTED_PACKET_LENGTH");
                    f.setAccessible(true);
                    return f;
                } catch (NoSuchFieldException e) {
                    throw new Error(e);
                }
            } else {
                return null;
            }
        }

        private static long OPENSSL_DESTROYED_OFFSET() {
            if (SslContext.OPENSSL_AVAILABLE) {
                return Unsafe.fieldOffset(OPENSSL_DESTROYED);
            } else {
                return -1;
            }
        }

        private static long OPENSSL_RECEIVED_SHUTDOWN_OFFSET() {
            if (SslContext.OPENSSL_AVAILABLE) {
                return Unsafe.fieldOffset(OPENSSL_RECEIVED_SHUTDOWN);
            } else {
                return -1;
            }
        }

        static boolean isOpensslEngineDestroyed(SSLEngine sslEngine) {
            if (ENABLE_OPENSSL_UNSAFE) {
                return isOpensslEngineDestroyedUnsafe(sslEngine);
            } else {
                return isOpensslEngineDestroyedField(sslEngine);
            }
        }

        static int MAX_ENCRYPTED_PACKET_LENGTH() {
            try {
                return ((Integer) OPENSSL_MAX_ENCRYPTED_PACKET_LENGTH.get(null));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        static boolean isOpensslEngineDestroyedField(SSLEngine sslEngine) {
            try {
                return ((Integer) OPENSSL_DESTROYED.get(sslEngine)) != 0;
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        static boolean isOpensslEngineDestroyedUnsafe(SSLEngine sslEngine) {
            return Unsafe.getIntVolatile(sslEngine, OPENSSL_DESTROYED_OFFSET) != 0;
        }

        static void setOpensslEngineReceivedShutdown(SSLEngine sslEngine) {
            if (ENABLE_OPENSSL_UNSAFE) {
                setOpensslEngineReceivedShutdownUnsafe(sslEngine);
            } else {
                setOpensslEngineReceivedShutdownField(sslEngine);
            }
        }

        static void setOpensslEngineReceivedShutdownField(SSLEngine sslEngine) {
            try {
                OPENSSL_RECEIVED_SHUTDOWN.set(sslEngine, true);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        static void setOpensslEngineReceivedShutdownUnsafe(SSLEngine sslEngine) {
            Unsafe.putBoolean(sslEngine, OPENSSL_RECEIVED_SHUTDOWN_OFFSET, true);
        }

    }

}

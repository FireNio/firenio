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

import com.firenio.buffer.ByteBuf;
import com.firenio.common.Util;
import com.firenio.concurrent.EventLoop;
import com.firenio.log.Logger;
import com.firenio.log.LoggerFactory;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.Status;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLException;
import java.io.IOException;

import static com.firenio.Develop.SSL_DEBUG;
import static com.firenio.Develop.debugException;
import static com.firenio.common.Util.unknownStackTrace;

/**
 * @author wangkai
 */
public abstract class ProtocolCodec {

    public static final int          SSL_PACKET_LIMIT      = 1024 * 64;
    public static final SSLException NOT_TLS               = newNOT_TLS();
    public static final IOException  TASK_REJECT           = newTASK_REJECT();
    public static final SSLException SSL_UNWRAP_CLOSED     = newSSL_UNWRAP_CLOSED();
    public static final SSLException SSL_UNWRAP_OVER_LIMIT = newSSL_UNWRAP_OVER_LIMIT();
    public static final SSLException SSL_PACKET_OVER_LIMIT = newSSL_PACKET_OVER_LIMIT();
    public static final SSLException SSL_UNWRAP_EXCEPTION  = newSSL_UNWRAP_EXCEPTION();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static SSLException newNOT_TLS() {
        return unknownStackTrace(new SSLException("not tls"), Channel.class, "isEnoughSslUnwrap()");
    }

    private static SSLException newSSL_UNWRAP_EXCEPTION() {
        return unknownStackTrace(new SSLException("unwrap exception(enable debug to get detail)"), Channel.class, "unwrap()");
    }

    private static SSLException newSSL_PACKET_OVER_LIMIT() {
        return unknownStackTrace(new SSLException("over writeIndex (" + SSL_PACKET_LIMIT + ")"), Channel.class, "isEnoughSslUnwrap()");
    }

    private static SSLException newSSL_UNWRAP_CLOSED() {
        return unknownStackTrace(new SSLException("SSL_UNWRAP_CLOSED"), Channel.class, "unwrap()");
    }

    private static IOException newTASK_REJECT() {
        return unknownStackTrace(new IOException(), Channel.class, "accept_reject(...)");
    }

    private static SSLException newSSL_UNWRAP_OVER_LIMIT() {
        return unknownStackTrace(new SSLException("over writeIndex (SSL_UNWRAP_BUFFER_SIZE)"), Channel.class, "unwrap()");
    }

    protected static IOException EXCEPTION(String className, String method, String msg) {
        return unknownStackTrace(new IOException(msg), className, method);
    }

    protected static IOException EXCEPTION(String msg) {
        return EXCEPTION("decode", msg);
    }

    protected void read_plain(Channel ch) throws Exception {
        ByteBuf dst = ch.getEventLoop().getReadBuf().clear();
        for (; ; ) {
            ch.read_plain_remain(dst);
            if (!read_data(ch, dst)) {
                return;
            }
            read_buf(ch, dst);
            // for epoll et mode
            if (dst.hasWritableBytes()) {
                break;
            }
        }
    }

    protected void read_ssl(Channel ch) throws Exception {
        ByteBuf src = ch.getEventLoop().getReadBuf().clear();
        for (; ; ) {
            src.clear();
            ch.read_ssl_remain(src);
            if (!read_data(ch, src)) {
                return;
            }
            for (; ; ) {
                if (isEnoughSslUnwrap(src)) {
                    ByteBuf res = unwrap(ch, src);
                    if (res != null) {
                        read_buf(ch, res);
                    }
                    src.resetWriteIndex();
                    if (!src.hasReadableBytes()) {
                        break;
                    }
                } else {
                    if (src.hasReadableBytes()) {
                        ch.store_ssl_remain(src);
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

    private ByteBuf unwrap(Channel ch, ByteBuf src) throws IOException {
        SSLEngine ssl_engine = ch.getSSLEngine();
        ByteBuf   dst        = FastThreadLocal.get().getSslUnwrapBuf();
        if (ch.ssl_handshake_finished) {
            ch.read_plain_remain(dst);
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
            ch.sync_buf(src, dst);
            return dst;
        } else {
            for (; ; ) {
                dst.clear();
                SSLEngineResult result          = unwrap(ssl_engine, src, dst);
                HandshakeStatus handshakeStatus = result.getHandshakeStatus();
                ch.sync_buf(src, dst);
                if (handshakeStatus == HandshakeStatus.NEED_WRAP) {
                    ch.writeAndFlush(ByteBuf.empty());
                    return null;
                } else if (handshakeStatus == HandshakeStatus.NEED_TASK) {
                    ch.run_delegated_tasks(ssl_engine);
                    continue;
                } else if (handshakeStatus == HandshakeStatus.FINISHED) {
                    ch.finish_handshake();
                    return null;
                } else if (handshakeStatus == HandshakeStatus.NEED_UNWRAP) {
                    if (src.hasReadableBytes()) {
                        continue;
                    }
                    return null;
                } else if (handshakeStatus == HandshakeStatus.NOT_HANDSHAKING) {
                    // NOTICE: this handle may not the NOT_HANDSHAKING expected
                    // NOT_HANDSHAKING is mean closed in wildfly-openssl.unwrap, so do not do finish_handshake
                    // see: https://stackoverflow.com/questions/31149383/difference-between-not-handshaking-and-finished
                    if (SSL_DEBUG) {
                        logger.error("unwrap handle status: NOT_HANDSHAKING({})", this);
                    }
                    throw SSL_UNWRAP_CLOSED;
                } else if (result.getStatus() == Status.BUFFER_OVERFLOW) {
                    throw SSL_UNWRAP_OVER_LIMIT;
                }
            }
        }
    }

    protected SSLEngineResult unwrap(SSLEngine ssl_engine, ByteBuf src, ByteBuf dst) throws SSLException {
        try {
            return ssl_engine.unwrap(src.nioReadBuffer(), dst.nioWriteBuffer());
        } catch (SSLException e) {
            logger.error(e.getMessage());
            debugException(logger, e);
        }
        throw SSL_UNWRAP_EXCEPTION;
    }

    protected int read_data_no_store(Channel ch, ByteBuf dst) {
        int len = ch.read(dst);
        if (len < 1) {
            if (len == -1) {
                Util.close(ch);
                return -1;
            }
            return 0;
        }
        dst.skipWrite(len);
        return len;
    }

    protected void store_plain_remain(Channel ch, ByteBuf buf) {
        ch.store_plain_remain(buf);
    }

    protected void read_plain_remain(Channel ch, ByteBuf dst) {
        ch.read_plain_remain(dst);
    }

    protected boolean read_data(Channel ch, ByteBuf dst) {
        int len = ch.read(dst);
        if (len < 1) {
            if (len == -1) {
                Util.close(ch);
                return false;
            }
            if (ch.enable_ssl) {
                ch.store_ssl_remain(dst);
            } else {
                ch.store_plain_remain(dst);
            }
            return false;
        }
        dst.skipWrite(len);
        return true;
    }

    protected void read_buf(Channel ch, ByteBuf src) throws Exception {
        final IoEventHandle handle     = ch.getIoEventHandle();
        final EventLoop     eel        = ch.getExecutorEventLoop();
        final boolean       enable_wel = eel != null;
        for (; ; ) {
            Frame f = decode(ch, src);
            if (f == null) {
                ch.store_plain_remain(src);
                break;
            }
            if (enable_wel) {
                accept_async(ch, eel, f);
            } else {
                accept_line(ch, handle, f);
            }
            if (!src.hasReadableBytes()) {
                break;
            }
        }
    }

    protected void accept_async(final Channel ch, final EventLoop eel, final Frame f) {
        final Runnable job = () -> {
            try {
                ch.getIoEventHandle().accept(ch, f);
            } catch (Throwable e) {
                ch.getIoEventHandle().exceptionCaught(ch, f, e);
            }
        };
        if (!eel.submit(job)) {
            exception_caught(ch, ch.getIoEventHandle(), f, TASK_REJECT);
        }
    }

    protected void accept_line(Channel ch, IoEventHandle handle, Frame frame) {
        try {
            handle.accept(ch, frame);
        } catch (Throwable e) {
            exception_caught(ch, handle, frame, e);
        }
    }

    private void exception_caught(Channel ch, IoEventHandle handle, Frame frame, Throwable ex) {
        try {
            handle.exceptionCaught(ch, frame, ex);
        } catch (Throwable e) {
            logger.error(ex.getMessage(), ex);
            logger.error(e.getMessage(), e);
        }
    }

    protected static IOException EXCEPTION(String method, String msg) {
        String              className = null;
        StackTraceElement[] sts       = Thread.currentThread().getStackTrace();
        if (sts.length > 1) {
            String thisClassName = ProtocolCodec.class.getName();
            for (int i = 1; i < sts.length; i++) {
                String name = sts[i].getClassName();
                if (!name.equals(thisClassName)) {
                    className = name;
                    break;
                }
            }
        }
        if (className == null) {
            className = ProtocolCodec.class.getName();
        }
        return EXCEPTION(className, method, msg);
    }

    // 可能会遭受一种攻击，比如最大可接收数据为100，客户端传输到99后暂停，
    // 这样多次以后可能会导致内存溢出
    public Frame decode(Channel ch, ByteBuf src) throws Exception {
        throw new UnsupportedOperationException();
    }

    // 注意：encode失败要release掉encode过程中申请的内存
    public ByteBuf encode(Channel ch, Frame frame) throws Exception {
        Object  content = frame.getContent();
        ByteBuf buf;
        if (content instanceof ByteBuf) {
            buf = (ByteBuf) content;
        } else {
            byte[] data = (byte[]) content;
            buf = ch.allocateWithSkipHeader(data.length);
            buf.writeBytes(data);
        }
        encode(ch, frame, buf);
        return buf;
    }

    protected void encode(Channel ch, Frame frame, ByteBuf buf) throws Exception {
        throw new UnsupportedOperationException();
    }

    public String getProtocolId() {
        return "Codec";
    }

    public int getHeaderLength() {
        return 0;
    }

    protected Object newAttachment() {
        return null;
    }

    public void release(NioEventLoop eventLoop, Frame frame) {
    }

    protected void flush_ping(Channel ch) {
        ByteBuf buf = getPingBuf();
        if (buf != null) {
            ch.writeAndFlush(buf);
            ch.getContext().getHeartBeatLogger().logPingTo(ch);
        } else {
            // 该channel无需心跳,比如HTTP协议
        }
    }

    protected void log_ping_from(Channel ch) {
        ch.getContext().getHeartBeatLogger().logPingFrom(ch);
    }

    protected void log_pong_from(Channel ch) {
        ch.getContext().getHeartBeatLogger().logPongFrom(ch);
    }

    protected void flush_pong(Channel ch, ByteBuf buf) {
        ch.writeAndFlush(buf);
        ch.getContext().getHeartBeatLogger().logPongTo(ch);
    }

    protected ByteBuf getPingBuf() {
        return null;
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

}

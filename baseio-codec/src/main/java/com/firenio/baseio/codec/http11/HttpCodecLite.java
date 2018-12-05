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
package com.firenio.baseio.codec.http11;

import java.io.IOException;
import java.util.Map;

import com.firenio.baseio.buffer.ByteBuf;
import com.firenio.baseio.buffer.ByteBufUtil;
import com.firenio.baseio.collection.IntEntry;
import com.firenio.baseio.collection.IntMap;
import com.firenio.baseio.common.DateUtil;
import com.firenio.baseio.common.KMPUtil;
import com.firenio.baseio.common.Util;
import com.firenio.baseio.component.ChannelContext;
import com.firenio.baseio.component.FastThreadLocal;
import com.firenio.baseio.component.NioEventLoop;
import com.firenio.baseio.component.NioSocketChannel;
import com.firenio.baseio.protocol.Frame;
import com.firenio.baseio.protocol.ProtocolCodec;

/**
 * @author wangkai
 *
 */
public class HttpCodecLite extends ProtocolCodec {

    static final byte[]            CONTENT_LENGTH_MATCH  = b("Content-Length:");
    static final byte[]            DATE                  = b("\r\nDate: ");
    static final byte[]            CONNECTION            = b("\r\nConnection: ");
    static final byte[]            CONTENT_TYPE          = b("\r\nContent-Type: ");
    static final int               dbshIndex             = nextIndexedVariablesIndex();
    static final int               decode_state_body     = 2;
    static final int               decode_state_complate = 3;
    static final int               decode_state_header   = 1;
    static final int               decode_state_line_one = 0;
    static final String            FRAME_BUFFER_KEY      = "_HTTP_FRAME_BUFFER_KEY";
    static final String            FRAME_DECODE_KEY      = "_HTTP_FRAME_DECODE_KEY";
    static final HttpDateTimeClock httpDateTimeClock     = new HttpDateTimeClock();
    static final KMPUtil           KMP_BOUNDARY          = new KMPUtil("boundary=");
    static final byte              N                     = '\n';
    static final byte              R                     = '\r';
    static final byte              SPACE                 = ' ';

    private final byte[]           PROTOCOL;
    private int                    bodyLimit             = 1024 * 512;
    private int                    headerLimit           = 1024 * 8;
    private int                    frameBuffer           = 0;

    public HttpCodecLite(String server) {
        this(server, 0);
    }

    public HttpCodecLite(String server, int frameBuffer) {
        this.frameBuffer = frameBuffer;
        if (server == null) {
            this.PROTOCOL = b("HTTP/1.1 200 OK\r\nContent-Length: ");
        } else {
            this.PROTOCOL = b("HTTP/1.1 200 OK\r\nServer: " + server + "\r\nContent-Length: ");
        }
    }

    private HttpFrameLite allocHttpFrame(NioSocketChannel ch) {
        HttpFrameLite f = (HttpFrameLite) ch.getAttribute(FRAME_DECODE_KEY);
        if (f == null) {
            if (frameBuffer > 0) {
                NioEventLoop el = ch.getEventLoop();
                Frame res = el.getFrameFromBuffer(ch, FRAME_BUFFER_KEY, frameBuffer);
                if (res == null) {
                    return new HttpFrameLite();
                } else {
                    return (HttpFrameLite) res.reset();
                }
            }
            return new HttpFrameLite();
        }
        return f;
    }

    @Override
    public Frame decode(NioSocketChannel ch, ByteBuf src) throws IOException {
        HttpFrameLite f = allocHttpFrame(ch);
        int decode_state = f.getDecodeState();
        if (decode_state == decode_state_line_one) {
            int ln = src.indexOf(N);
            if (ln != -1) {
                int p = src.absPos();
                f.incrementHeaderLength(ln - p);
                decode_state = decode_state_header;
                ln = findN(src, ln); 
                int skip;
                if (src.absByte(p) == 'G') {
                    f.setMethod(HttpMethod.GET);
                    skip = 4;
                }else{
                    f.setMethod(HttpMethod.POST);
                    skip = 5;
                }
                StringBuilder line = FastThreadLocal.get().getStringBuilder();
                int count = ln - 9;
                for (int i = p + skip; i < count; i++) {
                    line.append((char) (src.absByte(i) & 0xff));
                }
                int qmask = Util.indexOf(line, '?');
                if (qmask > -1) {
                    parse_kv(f.getRequestParams(), line, qmask + 1, line.length(), '=', '&');
                    f.setRequestURL((String) line.subSequence(0, qmask));
                } else {
                    f.setRequestURL(line.toString());
                }
            }
        }
        if (decode_state == decode_state_header) {
            for (;;) {
                int ps = src.absPos();
                int pe = read_line_range(src, f.getHeaderLength(), headerLimit);
                if (pe == -1) {
                    break;
                }
                int size = pe - ps;
                f.incrementHeaderLength(size);
                if (size == 0) {
                    if (f.getContentLength() < 1) {
                        decode_state = decode_state_complate;
                    } else {
                        if (f.getContentLength() > bodyLimit) {
                            throw new IOException("over limit:" + bodyLimit);
                        }
                        decode_state = decode_state_body;
                    }
                    break;
                } else {
                    if (!f.isGet()) {
                        if (startWith(src, ps, pe, CONTENT_LENGTH_MATCH)) {
                            ByteBufUtil.skip(src, SPACE);
                            int ctLen = 0;
                            for (int i = src.absPos(); i < pe; i++) {
                                ctLen = (src.getByte() - '0') * 10 + ctLen;
                            }
                        }
                    }
                }
            }
        }
        if (decode_state == decode_state_body) {
            int contentLength = f.getContentLength();
            int remain = src.remaining();
            if (remain > contentLength) {
                src.markL();
                src.limit(src.position() + contentLength);
                f.setContent(src.getBytes());
                src.resetL();
                decode_state = decode_state_complate;
            } else if (remain == contentLength) {
                f.setContent(src.getBytes());
                decode_state = decode_state_complate;
            }
        }
        if (decode_state == decode_state_complate) {
            ch.removeAttribute(FRAME_DECODE_KEY);
            return f;
        } else {
            f.setDecodeState(decode_state);
            ch.setAttribute(FRAME_DECODE_KEY, f);
            return null;
        }
    }

    @Override
    public void destory(ChannelContext context) {
        httpDateTimeClock.stop();
    }

    @Override
    public ByteBuf encode(final NioSocketChannel ch, Frame frame) throws IOException {
        HttpFrameLite f = (HttpFrameLite) frame;
        FastThreadLocal l = FastThreadLocal.get();
        byte[] byte32 = l.getBytes32();
        byte[] date_bytes = getHttpDateBytes();
        byte[] conn_bytes = f.getConnection();
        byte[] type_bytes = f.getContentType();
        int write_size = f.getWriteSize();
        int len_idx = Util.valueOf(write_size, byte32);
        int len_len = 32 - len_idx;
        int plen = PROTOCOL.length;
        int dlen = DATE.length;
        int conn_len = conn_bytes == null ? 0 : conn_bytes.length + CONNECTION.length;
        int type_len = type_bytes == null ? 0 : type_bytes.length + CONTENT_TYPE.length;
        int len = plen + len_len + dlen + date_bytes.length + 2 + conn_len + type_len;
        IntMap<byte[]> headers = f.getResponseHeaders();
        if (headers != null) {
            for (IntEntry<byte[]> header : headers.entries()) {
                byte[] k = HttpHeader.get(header.key()).getBytes();
                byte[] v = header.value();
                len += 4;
                len += k.length;
                len += v.length;
            }
        }
        len += 2;
        len += write_size;
        ByteBuf buf = ch.alloc().allocate(len);
        buf.put(PROTOCOL);
        buf.put(byte32, len_idx, len_len);
        buf.put(DATE);
        buf.put(date_bytes);
        if (conn_bytes != null) {
            buf.put(CONNECTION);
            buf.put(conn_bytes);
        }
        if (type_bytes != null) {
            buf.put(CONTENT_TYPE);
            buf.put(type_bytes);
        }
        buf.putByte(R);
        buf.putByte(N);
        if (headers != null) {
            for (IntEntry<byte[]> header : headers.entries()) {
                byte[] k = HttpHeader.get(header.key()).getBytes();
                byte[] v = header.value();
                buf.put(k);
                buf.putByte((byte) ':');
                buf.putByte(SPACE);
                buf.put(v);
                buf.putByte(R);
                buf.putByte(N);
            }
        }
        buf.putByte(R);
        buf.putByte(N);
        if (write_size != 0) {
            buf.put(f.getWriteBuffer(), 0, write_size);
        }
        return buf.flip();
    }

    public int getBodyLimit() {
        return bodyLimit;
    }

    public int getHeaderLimit() {
        return headerLimit;
    }

    private byte[] getHttpDateBytes() {
        DBsH h = (DBsH) FastThreadLocal.get().getIndexedVariable(dbshIndex);
        if (h == null) {
            h = new DBsH();
            FastThreadLocal.get().setIndexedVariable(dbshIndex, h);
        }
        long now = httpDateTimeClock.time;
        if (now > h.time) {
            h.time = now + 1000;
            h.value = DateUtil.get().formatHttpBytes(now);
        }
        return h.value;
    }

    public int getHttpFrameStackSize() {
        return frameBuffer;
    }

    @Override
    public String getProtocolId() {
        return "HTTP1.1";
    }

    @Override
    public void initialize(ChannelContext context) throws Exception {
        Util.exec(httpDateTimeClock, "HttpDateTimeClock");
    }

    public void release(NioEventLoop eventLoop, Frame frame) {
        eventLoop.releaseFrame(FRAME_BUFFER_KEY, frame);
    }

    private boolean startWith(ByteBuf src, int ps, int pe, byte[] match) {
        if (pe - ps < match.length) {
            return false;
        }
        for (int i = 0; i < match.length; i++) {
            if (src.absByte(ps + i) != match[i]) {
                return false;
            }
        }
        return true;
    }

    private class DBsH {
        long   time;
        byte[] value;
    }

    private static class HttpDateTimeClock implements Runnable {

        volatile boolean running;

        volatile long    time;

        @Override
        public void run() {
            running = true;
            for (; running;) {
                time = System.currentTimeMillis();
                Util.sleep(1000);
            }
        }

        void stop() {
            running = false;
            Thread.currentThread().interrupt();
        }

    }

    private static byte[] b(String s) {
        return s.getBytes();
    }

    public static void parse_kv(Map<String, String> map, CharSequence line, int start, int end,
            char kvSplitor, char eSplitor) {
        int state_findKey = 0;
        int state_findValue = 1;
        int state = state_findKey;
        int count = end;
        int i = start;
        int ks = start;
        int vs = 0;
        CharSequence key = null;
        CharSequence value = null;
        for (; i != count;) {
            char c = line.charAt(i++);
            if (state == state_findKey) {
                if (c == kvSplitor) {
                    key = line.subSequence(ks, i - 1);
                    state = state_findValue;
                    vs = i;
                    continue;
                }
            } else if (state == state_findValue) {
                if (c == eSplitor) {
                    value = line.subSequence(vs, i - 1);
                    state = state_findKey;
                    ks = i;
                    map.put((String) key, (String) value);
                    continue;
                }
            }
        }
        if (state == state_findValue && end > vs) {
            map.put((String) key, (String) line.subSequence(vs, end));
        }
    }

    private static int read_line_range(ByteBuf src, int length, int limit) throws IOException {
        src.markP();
        int p;
        int maybeRead = limit - length;
        if (src.remaining() > maybeRead) {
            p = src.indexOf(N, src.absPos(), maybeRead);
            if (p == -1) {
                throw new IOException("max http header length " + limit);
            }
        } else {
            p = src.indexOf(N);
            if (p == -1) {
                src.resetP();
                return -1;
            }
        }
        return findN(src, p);
    }

    private static int findN(ByteBuf src, int p) {
        src.absPos(p + 1);
        p--;
        if (src.absByte(p) == R) {
            return p;
        } else {
            return ++p;
        }
    }

}

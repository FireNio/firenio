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

import static com.firenio.baseio.codec.http11.HttpHeader.Content_Length;
import static com.firenio.baseio.codec.http11.HttpHeader.Content_Type;
import static com.firenio.baseio.codec.http11.HttpHeader.Cookie;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.firenio.baseio.buffer.ByteBuf;
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
public class HttpCodec extends ProtocolCodec {

    static final byte[]            DATE                      = b("\r\nDate: ");
    static final byte[]            CONNECTION                = b("\r\nConnection: ");
    static final byte[]            CONTENT_TYPE              = b("\r\nContent-Type: ");
    static final int               dbshIndex                 = nextIndexedVariablesIndex();
    static final int               decode_state_body         = 2;
    static final int               decode_state_complate     = 3;
    static final int               decode_state_header       = 1;
    static final int               decode_state_line_one     = 0;
    static final int               encode_bytes_arrays_index = nextIndexedVariablesIndex();
    static final String            FRAME_BUFFER_KEY          = "_HTTP_FRAME_BUFFER_KEY";
    static final String            FRAME_DECODE_KEY          = "_HTTP_FRAME_DECODE_KEY";
    static final HttpDateTimeClock httpDateTimeClock         = new HttpDateTimeClock();
    static final KMPUtil           KMP_BOUNDARY              = new KMPUtil("boundary=");
    static final byte              N                         = '\n';
    static final byte[]            PROTOCOL                  = b("HTTP/1.1 ");
    static final byte              R                         = '\r';
    static final byte[]            SET_COOKIE                = b("Set-Cookie:");
    static final byte              SPACE                     = ' ';

    private final byte[]           CONTENT_LENGTH;
    private int                    bodyLimit                 = 1024 * 512;
    private int                    headerLimit               = 1024 * 8;
    private int                    httpFrameBuffer           = 0;
    private int                    websocketLimit            = 1024 * 128;
    private int                    websocketStackSize        = 0;

    public HttpCodec() {
        this(0);
    }

    public HttpCodec(String server) {
        this(server, 0);
    }

    public HttpCodec(int httpFrameBuffer) {
        this(null, httpFrameBuffer);
    }

    public HttpCodec(String server, int httpFrameBuffer) {
        this.httpFrameBuffer = httpFrameBuffer;
        if (server == null) {
            this.CONTENT_LENGTH = b("\r\nContent-Length: ");
        } else {
            this.CONTENT_LENGTH = b("\r\nServer: " + server + "\r\nContent-Length: ");
        }
    }

    HttpFrame allocHttpFrame(NioSocketChannel ch) {
        HttpFrame f = (HttpFrame) ch.getAttribute(FRAME_DECODE_KEY);
        if (f == null) {
            if (httpFrameBuffer > 0) {
                NioEventLoop el = ch.getEventLoop();
                Frame res = el.getFrameFromBuffer(ch, FRAME_BUFFER_KEY, httpFrameBuffer);
                if (res == null) {
                    return new HttpFrame();
                } else {
                    return (HttpFrame) res.reset();
                }
            }
            return new HttpFrame();
        }
        return f;
    }

    @Override
    public Frame decode(NioSocketChannel ch, ByteBuf src) throws IOException {
        HttpFrame f = allocHttpFrame(ch);
        StringBuilder line = FastThreadLocal.get().getStringBuilder();
        int decode_state = f.getDecodeState();
        if (decode_state == decode_state_line_one) {
            if (read_line(line, src, 0, headerLimit)) {
                f.incrementHeaderLength(line.length());
                decode_state = decode_state_header;
                parse_line_one(f, line);
            }
        }
        if (decode_state == decode_state_header) {
            for (;;) {
                line.setLength(0);
                if (!read_line(line, src, f.getHeaderLength(), headerLimit)) {
                    break;
                }
                f.incrementHeaderLength(line.length());
                if (line.length() == 0) {
                    decode_state = onHeaderReadComplete(f);
                    break;
                } else {
                    int p = Util.indexOf(line, ':');
                    if (p == -1) {
                        continue;
                    }
                    int rp = Util.skip(line, ' ', p + 1);
                    String name = line.substring(0, p);
                    String value = line.substring(rp);
                    f.setReadHeader(name, value);
                }
            }
        }
        if (decode_state == decode_state_body) {
            decode_state = decodeRemainBody(ch, src, f);
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

    int decodeRemainBody(NioSocketChannel ch, ByteBuf src, HttpFrame f) {
        int contentLength = f.getContentLength();
        int remain = src.remaining();
        byte[] ontent = null;
        if (remain < contentLength) {
            return decode_state_body;
        } else if (remain == contentLength) {
            ontent = src.getBytes();
        } else {
            src.markL();
            src.limit(src.position() + contentLength);
            ontent = src.getBytes();
            src.resetL();
        }
        if (!f.isForm()) {
            String param = new String(ontent, ch.getCharset());
            parse_kv(f.getRequestParams(), param, 0, param.length(), '=', '&');
        } else {
            f.setContent(ontent);
        }
        return decode_state_complate;
    }

    @Override
    public void destory(ChannelContext context) {
        httpDateTimeClock.stop();
    }

    @Override
    public ByteBuf encode(final NioSocketChannel ch, Frame frame) throws IOException {
        HttpFrame f = (HttpFrame) frame;
        byte[] byte32 = FastThreadLocal.get().getBytes32();
        byte[] status_bytes = f.getStatus().getBinary();
        byte[] date_bytes = getHttpDateBytes();
        byte[] conn_bytes = f.getConnection();
        byte[] type_bytes = f.getContentType();
        int write_size = f.getWriteSize();
        int len_idx = Util.valueOf(write_size, byte32);
        int len_len = 32 - len_idx;
        int plen = PROTOCOL.length;
        int clen = CONTENT_LENGTH.length;
        int dlen = DATE.length;
        int status_len = status_bytes.length;
        int conn_len = conn_bytes == null ? 0 : conn_bytes.length + CONNECTION.length;
        int type_len = type_bytes == null ? 0 : type_bytes.length + CONTENT_TYPE.length;
        int len = plen + status_len + clen + len_len + dlen + date_bytes.length + 2 + conn_len
                + type_len;
        int header_size = 0;
        int cookie_size = 0;
        List<byte[]> encode_bytes_array = getEncodeBytesArray();
        IntMap<byte[]> headers = f.getResponseHeaders();
        if (headers != null) {
            for (IntEntry<byte[]> header : headers.entries()) {
                byte[] k = HttpHeader.get(header.key()).getBytes();
                byte[] v = header.value();
                header_size++;
                encode_bytes_array.add(k);
                encode_bytes_array.add(v);
                len += 4;
                len += k.length;
                len += v.length;
            }
        }
        List<Cookie> cookieList = f.getCookieList();
        if (cookieList != null) {
            for (Cookie c : cookieList) {
                byte[] bytes = c.toString().getBytes();
                cookie_size++;
                encode_bytes_array.add(bytes);
                len += SET_COOKIE.length + 2;
                len += bytes.length;
            }
        }
        len += 2;
        len += write_size;
        ByteBuf buf = ch.alloc().allocate(len);
        buf.put(PROTOCOL);
        buf.put(status_bytes);
        buf.put(CONTENT_LENGTH);
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
        int j = 0;
        if (header_size > 0) {
            for (int i = 0; i < header_size; i++) {
                buf.put(encode_bytes_array.get(j++));
                buf.putByte((byte) ':');
                buf.putByte(SPACE);
                buf.put(encode_bytes_array.get(j++));
                buf.putByte(R);
                buf.putByte(N);
            }
        }
        if (cookie_size > 0) {
            for (int i = 0; i < cookie_size; i++) {
                buf.put(SET_COOKIE);
                buf.put(encode_bytes_array.get(j++));
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
        return httpFrameBuffer;
    }

    @Override
    public String getProtocolId() {
        return "HTTP1.1";
    }

    public int getWebsocketFrameStackSize() {
        return websocketStackSize;
    }

    public int getWebsocketLimit() {
        return websocketLimit;
    }

    @Override
    public void initialize(ChannelContext context) throws Exception {
        WebSocketCodec.init(context, websocketLimit, websocketStackSize);
        Util.exec(httpDateTimeClock, "HttpDateTimeClock");
    }

    int onHeaderReadComplete(HttpFrame f) throws IOException {
        int contentLength = 0;
        String clength = f.getRequestHeader(Content_Length);
        String ctype = f.getRequestHeader(Content_Type);
        String cookie = f.getRequestHeader(Cookie);
        f.setForm(ctype == null ? false : ctype.startsWith("multipart/form-data;"));
        if (!Util.isNullOrBlank(clength)) {
            contentLength = Integer.parseInt(clength);
            f.setContentLength(contentLength);
        }
        if (!Util.isNullOrBlank(cookie)) {
            parse_cookies(f, cookie);
        }
        if (contentLength < 1) {
            return decode_state_complate;
        } else {
            if (contentLength > bodyLimit) {
                throw new IOException("over limit:" + clength);
            }
            return decode_state_body;
        }
    }

    private void parse_cookies(HttpFrame f, String line) {
        Map<String, String> cookies = f.getCookies();
        if (cookies == null) {
            cookies = new HashMap<>();
            f.setCookies(cookies);
        }
        parse_kv(cookies, line, 0, line.length(), '=', ';');
    }

    void parse_kv(Map<String, String> map, CharSequence line, int start, int end, char kvSplitor,
            char eSplitor) {
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

    protected void parse_line_one(HttpFrame f, CharSequence line) {
        if (line.charAt(0) == 'G' && line.charAt(1) == 'E' && line.charAt(2) == 'T') {
            f.setMethod(HttpMethod.GET);
            parseRequestURL(f, 4, line);
        } else {
            f.setMethod(HttpMethod.POST);
            parseRequestURL(f, 5, line);
        }
        f.setVersion(HttpVersion.HTTP1_1.getId());
    }

    protected void parseRequestURL(HttpFrame f, int skip, CharSequence line) {
        int index = Util.indexOf(line, '?');
        int lastSpace = Util.lastIndexOf(line, ' ');
        if (index > -1) {
            parse_kv(f.getRequestParams(), line, index + 1, lastSpace, '=', '&');
            f.setRequestURL((String) line.subSequence(skip, index));
        } else {
            f.setRequestURL((String) line.subSequence(skip, lastSpace));
        }
    }

    @Override
    public void release(NioEventLoop eventLoop, Frame frame) {
        eventLoop.releaseFrame(FRAME_BUFFER_KEY, frame);
    }

    public void setWebsocketFrameStackSize(int websocketFrameStackSize) {
        this.websocketStackSize = websocketFrameStackSize;
    }

    public void setWebsocketLimit(int websocketLimit) {
        this.websocketLimit = websocketLimit;
    }

    class DBsH {
        long   time;
        byte[] value;
    }

    static class HttpDateTimeClock implements Runnable {

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

    static byte[] b(String s) {
        return s.getBytes();
    }

    @SuppressWarnings("unchecked")
    static List<byte[]> getEncodeBytesArray() {
        return (List<byte[]>) FastThreadLocal.get().getList(encode_bytes_arrays_index);
    }

    protected static String parseBoundary(String contentType) {
        int index = KMP_BOUNDARY.match(contentType);
        if (index != -1) {
            return contentType.substring(index + 9);
        }
        return null;
    }

    private static boolean read_line(StringBuilder line, ByteBuf src, int length, int limit)
            throws IOException {
        src.markP();
        int maybeRead = limit - length;
        if (src.remaining() > maybeRead) {
            int count = src.absPos() + maybeRead;
            for (int i = src.absPos(); i < count; i++) {
                byte b = src.absByte(i);
                if (b == N) {
                    int p = line.length() - 1;
                    if (line.charAt(p) == R) {
                        line.setLength(p);
                    }
                    src.absPos(i + 1);
                    return true;
                } else {
                    line.append((char) (b & 0xff));
                }
            }
            throw new IOException("max http header length " + limit);
        } else {
            int count = src.absPos() + src.remaining();
            for (int i = src.absPos(); i < count; i++) {
                byte b = src.absByte(i);
                if (b == N) {
                    int p = line.length() - 1;
                    if (line.charAt(p) == R) {
                        line.setLength(p);
                    }
                    src.absPos(i + 1);
                    return true;
                } else {
                    line.append((char) (b & 0xff));
                }
            }
        }
        src.resetP();
        return false;
    }

}

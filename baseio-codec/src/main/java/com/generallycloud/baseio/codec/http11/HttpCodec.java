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
package com.generallycloud.baseio.codec.http11;

import static com.generallycloud.baseio.codec.http11.HttpHeader.Content_Length;
import static com.generallycloud.baseio.codec.http11.HttpHeader.Content_Type;
import static com.generallycloud.baseio.codec.http11.HttpHeader.Cookie;
import static com.generallycloud.baseio.codec.http11.HttpHeader.Date;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.common.DateUtil;
import com.generallycloud.baseio.common.KMPUtil;
import com.generallycloud.baseio.common.Util;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.FastThreadLocal;
import com.generallycloud.baseio.component.NioEventLoop;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.protocol.Frame;
import com.generallycloud.baseio.protocol.ProtocolCodec;

/**
 * @author wangkai
 *
 */
public class HttpCodec extends ProtocolCodec {

    static final byte[]                    CONTENT_LENGTH        = b("\r\nContent-Length: ");
    static final ThreadLocal<DBsH>         dateBytes             = new ThreadLocal<>();
    static final int                       decode_state_body     = 2;
    static final int                       decode_state_complate = 3;
    static final int                       decode_state_header   = 1;
    static final int                       decode_state_line_one = 0;
    static final ThreadLocal<List<byte[]>> encode_bytes_arrays   = new ThreadLocal<>();
    static final String                    FRAME_DECODE_KEY      = "FRAME_HTTP_DECODE_KEY";
    static final String                    FRAME_STACK_KEY       = "FRAME_HTTP_STACK_KEY";
    static final KMPUtil                   KMP_BOUNDARY          = new KMPUtil("boundary=");
    static final byte                      N                     = '\n';
    static final byte[]                    PROTOCOL              = b("HTTP/1.1 ");
    static final byte                      R                     = '\r';
    static final byte[]                    SET_COOKIE            = b("Set-Cookie:");
    static final byte                      SPACE                 = ' ';

    private int                            bodyLimit             = 1024 * 512;
    private int                            headerLimit           = 1024 * 8;
    private int                            httpFrameStackSize    = 0;
    private int                            websocketLimit        = 1024 * 128;
    private int                            websocketStackSize    = 0;

    public HttpCodec() {}

    public HttpCodec(int httpFrameStackSize) {
        this.httpFrameStackSize = httpFrameStackSize;
    }

    public HttpCodec(int headerLimit, int bodyLimit) {
        this.headerLimit = headerLimit;
        this.bodyLimit = bodyLimit;
    }

    public HttpCodec(int headerLimit, int bodyLimit, int httpFrameStackSize) {
        this.headerLimit = headerLimit;
        this.bodyLimit = bodyLimit;
        this.httpFrameStackSize = httpFrameStackSize;
    }

    @SuppressWarnings("unchecked")
    HttpFrame allocHttpFrame(NioSocketChannel ch) {
        HttpFrame f = (HttpFrame) ch.getAttribute(FRAME_DECODE_KEY);
        if (f == null) {
            if (httpFrameStackSize > 0) {
                NioEventLoop eventLoop = ch.getEventLoop();
                List<HttpFrame> stack = (List<HttpFrame>) eventLoop.getAttribute(FRAME_STACK_KEY);
                if (stack == null) {
                    stack = new ArrayList<>(httpFrameStackSize);
                    eventLoop.setAttribute(FRAME_STACK_KEY, stack);
                }
                if (stack.isEmpty()) {
                    return new HttpFrame();
                } else {
                    return stack.remove(stack.size() - 1).reset(ch);
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
        switch (f.decode_state) {
            case decode_state_line_one:
                if (readLine(line, src, f.headerLength)) {
                    f.headerLength += line.length();
                    f.decode_state = decode_state_header;
                    parseFirstLine(f, line);
                }
            case decode_state_header:
                for (;;) {
                    line.setLength(0);
                    if (readLine(line, src, f.headerLength)) {
                        f.headerLength += line.length();
                        if (line.length() == 0) {
                            int contentLength = 0;
                            String clength = f.getReadHeader(Content_Length);
                            String ctype = f.getReadHeader(Content_Type);
                            String cookie = f.getReadHeader(Cookie);
                            parseContentType(f, ctype);
                            if (!Util.isNullOrBlank(clength)) {
                                contentLength = Integer.parseInt(clength);
                                f.contentLength = contentLength;
                            }
                            if (!Util.isNullOrBlank(cookie)) {
                                parse_cookies(f, cookie);
                            }
                            if (contentLength < 1) {
                                f.decode_state = decode_state_complate;
                            } else {
                                if (contentLength > bodyLimit) {
                                    // maybe can write to a temp file
                                    throw new IOException("over limit:" + clength);
                                }
                                f.decode_state = decode_state_body;
                            }
                            break;
                        } else {
                            int p = Util.indexOf(line, ':');
                            if (p == -1) {
                                continue;
                            }
                            String name = line.substring(0, p).trim();
                            String value = line.substring(p + 1).trim();
                            f.setReadHeader(name, value);
                        }
                    }
                }
            case decode_state_body:
                decodeRemainBody(ch, src, f);
        }
        if (f.decode_state == decode_state_complate) {
            doCompplete(ch, f);
            return f;
        } else {
            ch.setAttribute(FRAME_DECODE_KEY, f);
            return null;
        }
    }

    void decodeRemainBody(NioSocketChannel ch, ByteBuf src, HttpFrame f) {
        int contentLength = f.contentLength;
        int remain = src.remaining();
        byte[] bodyArray = null;
        if (remain < contentLength) {
            return;
        } else if (remain == contentLength) {
            bodyArray = src.getBytes();
        } else {
            src.markL();
            src.limit(src.position() + contentLength);
            bodyArray = src.getBytes();
            src.resetL();
        }
        if (f.contentType == HttpContentType.URLENCODED.getId()) {
            String paramString = new String(bodyArray, ch.getCharset());
            parse_kv(f.params, paramString, '=', '&');
        } else {
            f.bodyArray = bodyArray;
        }
        f.decode_state = decode_state_complate;
    }

    void doCompplete(NioSocketChannel ch, HttpFrame f) {
        ch.removeAttribute(FRAME_DECODE_KEY);
    }

    @Override
    public ByteBuf encode(final NioSocketChannel ch, Frame frame) throws IOException {
        HttpFrame f = (HttpFrame) frame;
        f.setResponseHeader(Date, getHttpDateBytes());
        int write_size = f.getWriteSize();
        byte[] status_bytes = f.getStatus().getBinary();
        byte[] length_bytes = String.valueOf(write_size).getBytes();
        int len = PROTOCOL.length + status_bytes.length + CONTENT_LENGTH.length
                + length_bytes.length + 2;
        List<byte[]> encode_bytes_array = getEncodeBytesArray();
        int header_size = 0;
        int cookie_size = 0;
        Map<HttpHeader, byte[]> headers = f.getResponseHeaders();
        for (Entry<HttpHeader, byte[]> header : headers.entrySet()) {
            byte[] k = header.getKey().getBytes();
            byte[] v = header.getValue();
            if (v == null) {
                continue;
            }
            header_size++;
            encode_bytes_array.add(k);
            encode_bytes_array.add(v);
            len += 4;
            len += k.length;
            len += v.length;
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
        buf.put(length_bytes);
        buf.putByte(R);
        buf.putByte(N);
        int j = 0;
        for (int i = 0; i < header_size; i++) {
            buf.put(encode_bytes_array.get(j++));
            buf.putByte((byte) ':');
            buf.putByte(SPACE);
            buf.put(encode_bytes_array.get(j++));
            buf.putByte(R);
            buf.putByte(N);
        }
        for (int i = 0; i < cookie_size; i++) {
            buf.put(SET_COOKIE);
            buf.put(encode_bytes_array.get(j++));
            buf.putByte(R);
            buf.putByte(N);
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
        DBsH h = dateBytes.get();
        if (h == null) {
            h = new DBsH();
            dateBytes.set(h);
        }
        long now = System.currentTimeMillis();
        if (now > h.time) {
            h.time = now + 1000;
            h.value = DateUtil.get().formatHttpBytes(now);
        }
        return h.value;
    }

    public int getHttpFrameStackSize() {
        return httpFrameStackSize;
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
    public void initialize(ChannelContext context) {
        WebSocketCodec.init(context, websocketLimit, websocketStackSize);
    }

    private void parse_cookies(HttpFrame f, String line) {
        Map<String, String> cookies = f.cookies;
        if (cookies == null) {
            cookies = new HashMap<>();
            f.cookies = cookies;
        }
        parse_kv(cookies, line, '=', ';');
    }

    void parse_kv(Map<String, String> map, String line, char kvSplitor, char eSplitor) {
        StringBuilder sb = FastThreadLocal.get().getStringBuilder();
        int state_findKey = 0;
        int state_findValue = 1;
        int state = state_findKey;
        int count = line.length();
        int i = 0;
        String key = null;
        String value = null;
        for (; i != count;) {
            char c = line.charAt(i++);
            if (state == state_findKey) {
                if (c == kvSplitor) {
                    key = sb.toString();
                    state = state_findValue;
                    sb.setLength(0);
                    continue;
                } else if (c == ' ') {
                    continue;
                }
                sb.append(c);
            } else if (state == state_findValue) {
                if (c == eSplitor) {
                    value = sb.toString();
                    state = state_findKey;
                    map.put(key, value);
                    sb.setLength(0);
                    continue;
                }
                sb.append(c);
            }
        }
        if (state == state_findValue) {
            map.put(key, sb.toString());
        }
    }

    private void parseContentType(HttpFrame f, String contentType) {
        if (Util.isNullOrBlank(contentType)) {
            f.contentType = HttpContentType.URLENCODED.getId();
        } else if (HttpStatic.application_urlencoded.equals(f.contentType)) {
            f.contentType = HttpContentType.URLENCODED.getId();
        } else if (contentType.startsWith("multipart/form-data;")) {
            f.contentType = HttpContentType.MULTIPART.getId();
        }
    }

    protected void parseFirstLine(HttpFrame f, StringBuilder line) {
        if (line.charAt(0) == 'G' && line.charAt(1) == 'E' && line.charAt(2) == 'T'
                && line.charAt(3) == ' ') {
            f.setMethod(HttpMethod.GET);
            parseRequestURL(f, 4, line);
        } else {
            f.setMethod(HttpMethod.POST);
            parseRequestURL(f, 5, line);
        }
        f.setVersion(HttpVersion.HTTP1_1.getId());
    }

    protected void parseRequestURL(HttpFrame f, int skip, StringBuilder line) {
        int index = line.indexOf("?");
        int lastSpace = Util.lastIndexOf(line, ' ');
        if (index > -1) {
            String paramString = line.substring(index + 1, lastSpace);
            parse_kv(f.params, paramString, '=', '&');
            f.setRequestURI(line.substring(skip, index));
        } else {
            f.setRequestURI(line.substring(skip, lastSpace));
        }
    }

    private boolean readLine(StringBuilder line, ByteBuf src, int length) throws IOException {
        src.markP();
        for (; src.hasRemaining();) {
            if (++length > headerLimit) {
                throw new IOException("max http header length " + headerLimit);
            }
            byte b = src.getByte();
            if (b == N) {
                return true;
            } else if (b == R) {
                continue;
            } else {
                line.append((char) b);
            }
        }
        src.resetP();
        return false;
    }

    @SuppressWarnings("unchecked")
    public void release(NioEventLoop eventLoop, Frame frame) {
        //FIXME ..final statck is null or not null
        List<HttpFrame> stack = (List<HttpFrame>) eventLoop.getAttribute(FRAME_STACK_KEY);
        if (stack != null && stack.size() < httpFrameStackSize) {
            stack.add((HttpFrame) frame);
        }
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

    private static byte[] b(String s) {
        return s.getBytes();
    }

    static List<byte[]> getEncodeBytesArray() {
        List<byte[]> array = encode_bytes_arrays.get();
        if (array == null) {
            array = new ArrayList<>();
            encode_bytes_arrays.set(array);
        }
        array.clear();
        return array;
    }

    protected static String parseBoundary(String contentType) {
        int index = KMP_BOUNDARY.match(contentType);
        if (index != -1) {
            return contentType.substring(index + 9);
        }
        return null;
    }

}

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

import static com.generallycloud.baseio.codec.http11.HttpHeader.*;
import static com.generallycloud.baseio.codec.http11.HttpStatic.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.common.DateUtil;
import com.generallycloud.baseio.common.KMPUtil;
import com.generallycloud.baseio.common.StringUtil;
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

    private static final byte[]            CONTENT_LENGTH     = b("\r\nContent-Length: ");
    private static final ThreadLocal<DBsH> dateBytes          = new ThreadLocal<>();
    private static final String            FRAME_STACK_KEY    = "FRAME_HTTP_STACK_KEY";
    private static final String            FRAME_DECODE_KEY   = "FRAME_HTTP_DECODE_KEY";
    private static final KMPUtil           KMP_BOUNDARY       = new KMPUtil("boundary=");
    public static final byte               N                  = '\n';
    public static final byte[]             PROTOCOL           = b("HTTP/1.1 ");
    public static final byte               R                  = '\r';
    public static final byte[]             SET_COOKIE         = b("Set-Cookie:");
    public static final byte               SPACE              = ' ';

    private int                            bodyLimit          = 1024 * 512;
    private int                            headerLimit        = 1024 * 8;
    private int                            httpFrameStackSize = 0;
    private int                            websocketStackSize = 0;
    private int                            websocketLimit     = 1024 * 128;

    private static byte[] b(String s) {
        return s.getBytes();
    }

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

    @Override
    public Frame decode(NioSocketChannel ch, ByteBuf src) throws IOException {
        HttpFrame f = (HttpFrame) ch.getAttribute(FRAME_DECODE_KEY);
        if (f == null) {
            f = newHttpFrame(ch);
        }
        if (!f.header_complete) {
            decodeHeader(f, src);
            if (!f.header_complete) {
                setHttpFrame(ch, f);
                return null;
            }
            int contentLength = 0;
            String clStr = f.getReadHeader(Content_Length);
            if (!StringUtil.isNullOrBlank(clStr)) {
                f.contentLength = contentLength = Integer.parseInt(clStr);
            }
            String contentType = f.getReadHeader(Content_Type);
            parseContentType(f, contentType);
            String cookie = f.getReadHeader(Cookie);
            if (!StringUtil.isNullOrBlank(cookie)) {
                parse_cookies(f, cookie);
            }
            if (contentLength < 1) {
                doCompplete(ch, f);
                return f;
            } else {
                if (contentLength > bodyLimit) {
                    // FIXME 写入临时文件
                    throw new IOException("over limit:" + clStr);
                }
            }
        }
        return decodeRemainBody(ch, src, f);
    }

    Frame decodeRemainBody(NioSocketChannel ch, ByteBuf src, HttpFrame f) {
        int contentLength = f.contentLength;
        int remain = src.remaining();
        byte[] bodyArray = null;
        if (remain == contentLength) {
            bodyArray = src.getBytes();
        } else if (remain < contentLength) {
            setHttpFrame(ch, f);
            return null;
        } else {
            src.markL();
            src.limit(src.position() + contentLength);
            bodyArray = src.getBytes();
            src.resetL();
        }
        if (application_urlencoded.equals(f.contentType)) {
            // FIXME encoding
            String paramString = new String(bodyArray, ch.getCharset());
            parse_kv(f.params, paramString, '=', '&');
        } else {
            // FIXME 解析BODY中的内容
            f.bodyArray = bodyArray;
        }
        doCompplete(ch, f);
        return f;
    }

    void doCompplete(NioSocketChannel ch, HttpFrame f) {
        ch.removeAttribute(FRAME_DECODE_KEY);
    }

    static final ThreadLocal<List<byte[]>> encode_bytes_arrays = new ThreadLocal<>();

    static List<byte[]> getEncodeBytesArray() {
        List<byte[]> array = encode_bytes_arrays.get();
        if (array == null) {
            array = new ArrayList<>();
            encode_bytes_arrays.set(array);
        }
        array.clear();
        return array;
    }

    @Override
    public ByteBuf encode(NioSocketChannel ch, Frame frame) throws IOException {
        HttpFrame f = (HttpFrame) frame;
        if (f.isUpdateWebSocketProtocol()) {
            ch.setCodec(WebSocketCodec.WS_PROTOCOL_CODEC);
            ch.setAttribute(WebSocketCodec.CHANNEL_KEY_SERVICE_NAME, f.getFrameName());
        }
        f.setResponseHeader(Date, getHttpDateBytes());
        int write_size = f.getWriteSize();
        byte[] status_bytes = f.getStatus().getBinary();
        byte[] length_bytes = String.valueOf(write_size).getBytes();
        int len = PROTOCOL.length 
                + status_bytes.length 
                + CONTENT_LENGTH.length
                + length_bytes.length 
                + 2;
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

    @SuppressWarnings("unchecked")
    HttpFrame newHttpFrame(NioSocketChannel ch) {
        if (httpFrameStackSize > 0) {
            NioEventLoop eventLoop = ch.getEventLoop();
            List<HttpFrame> stack = (List<HttpFrame>) eventLoop.getAttribute(FRAME_STACK_KEY);
            if (stack == null) {
                stack = new ArrayList<>(httpFrameStackSize);
                eventLoop.setAttribute(FRAME_STACK_KEY, stack);
            }
            if (stack.isEmpty()) {
                return new HttpFrame(ch.getContext());
            } else {
                return stack.remove(stack.size() - 1).reset(ch);
            }
        }
        return new HttpFrame(ch.getContext());
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
        if (StringUtil.isNullOrBlank(contentType)) {
            f.contentType = application_urlencoded;
            return;
        }
        if (contentType.startsWith("multipart/form-data;")) {
            int index = KMP_BOUNDARY.match(contentType);
            if (index != -1) {
                f.setBoundary(contentType.substring(index + 9));
            }
            f.contentType = multipart;
        } else {
            // FIXME other content-type
            f.contentType = contentType;
        }
    }

    protected void parseRequestURL(HttpFrame f, int skip, StringBuilder line) {
        int index = line.indexOf("?");
        int lastSpace = StringUtil.lastIndexOf(line, ' ');
        if (index > -1) {
            String paramString = line.substring(index + 1, lastSpace);
            parse_kv(f.params, paramString, '=', '&');
            f.setRequestURI(line.substring(skip, index));
        } else {
            f.setRequestURI(line.substring(skip, lastSpace));
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
        f.setVersion(HttpVersion.HTTP1_1);
    }

    private void decodeHeader(HttpFrame f, ByteBuf buffer) throws IOException {
        StringBuilder currentHeaderLine = f.currentHeaderLine;
        if (currentHeaderLine == null) {
            currentHeaderLine = FastThreadLocal.get().getStringBuilder();
        }
        int headerLength = f.headerLength;
        for (; buffer.hasRemaining();) {
            if (++headerLength > headerLimit) {
                throw new IOException("max http header length " + headerLimit);
            }
            byte b = buffer.getByte();
            if (b == N) {
                if (currentHeaderLine.length() == 0) {
                    f.header_complete = true;
                    break;
                } else {
                    if (f.parseFirstLine) {
                        f.parseFirstLine = false;
                        parseFirstLine(f, currentHeaderLine);
                    } else {
                        int p = StringUtil.indexOf(currentHeaderLine, ':');
                        if (p == -1) {
                            continue;
                        }
                        String name = currentHeaderLine.substring(0, p).trim();
                        String value = currentHeaderLine.substring(p + 1).trim();
                        f.setReadHeader(name, value);
                    }
                    currentHeaderLine.setLength(0);
                }
                continue;
            } else if (b == R) {
                continue;
            } else {
                currentHeaderLine.append((char) b);
            }
        }
        if (!f.header_complete) {
            f.headerLength = headerLength;
            if (f.currentHeaderLine == null) {
                f.currentHeaderLine = new StringBuilder(currentHeaderLine.length() + 32);
                f.currentHeaderLine.append(currentHeaderLine);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void release(NioEventLoop eventLoop, Frame frame) {
        //FIXME ..final statck is null or not null
        List<HttpFrame> stack = (List<HttpFrame>) eventLoop.getAttribute(FRAME_STACK_KEY);
        if (stack != null && stack.size() < httpFrameStackSize) {
            stack.add((HttpFrame) frame);
        }
    }

    void setHttpFrame(NioSocketChannel ch, HttpFrame f) {
        ch.setAttribute(FRAME_DECODE_KEY, f);
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

}

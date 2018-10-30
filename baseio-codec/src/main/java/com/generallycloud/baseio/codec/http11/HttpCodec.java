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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.ByteBufAllocator;
import com.generallycloud.baseio.common.DateUtil;
import com.generallycloud.baseio.common.KMPUtil;
import com.generallycloud.baseio.common.StringLexer;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.NioEventLoop;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.protocol.Frame;
import com.generallycloud.baseio.protocol.ProtocolCodec;
import static com.generallycloud.baseio.codec.http11.HttpHeader.*;

/**
 * @author wangkai
 *
 */
public class HttpCodec extends ProtocolCodec {

    public static final byte                        COLON                              = ':';
    public static final String                      CONTENT_APPLICATION_JAVASCRIPTUTF8 = "application/x-javascript;charset=utf-8";
    public static final String                      CONTENT_APPLICATION_OCTET_STREAM   = "application/octet-stream";
    public static final String                      CONTENT_APPLICATION_URLENCODED     = "application/x-www-form-urlencoded";
    public static final byte[]                      CONTENT_LENGTH                     = "\r\nContent-Length: ".getBytes();
    public static final String                      CONTENT_TYPE_IMAGE_GIF             = "image/gif";
    public static final String                      CONTENT_TYPE_IMAGE_ICON            = "image/x-icon";
    public static final String                      CONTENT_TYPE_IMAGE_JPEG            = "image/jpeg";
    public static final String                      CONTENT_TYPE_IMAGE_PNG             = "image/png";
    public static final String                      CONTENT_TYPE_MULTIPART             = "multipart/form-data";
    public static final String                      CONTENT_TYPE_TEXT_CSSUTF8          = "text/css;charset=utf-8";
    public static final String                      CONTENT_TYPE_TEXT_HTMLUTF8         = "text/html;charset=utf-8";
    public static final String                      CONTENT_TYPE_TEXT_PLAIN            = "text/plain";
    public static final String                      CONTENT_TYPE_TEXT_PLAINUTF8        = "text/plain;charset=utf-8";
    private static final ThreadLocal<HDBsHolder>    dateBytes                          = new ThreadLocal<>();
    public static final String                      FRAME_STACK_KEY                    = "FixedThreadStack_HttpFrame";
    private static final String                     HTTP_DECODE_FRAME_KEY              = "_HTTP_DECODE_FRAME_KEY";
    private static final KMPUtil                    KMP_BOUNDARY                       = new KMPUtil("boundary=");
    public static final byte                        N                                  = '\n';
    public static final byte[]                      PROTOCOL                           = "HTTP/1.1 ".getBytes();
    public static final byte                        R                                  = '\r';
    public static final byte[]                      SET_COOKIE                         = "Set-Cookie:".getBytes();
    public static final byte                        SPACE                              = ' ';
    private static final ThreadLocal<StringBuilder> stringBuilder                      = new ThreadLocal<>();

    private int                                     bodyLimit                          = 1024 * 512;
    private int                                     headerLimit                        = 1024 * 8;
    private int                                     httpFrameStackSize                 = 0;
    private int                                     websocketFrameStackSize            = 0;
    private int                                     websocketLimit                     = 1024 * 128;

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
        HttpFrame f = (HttpFrame) ch.getAttribute(HTTP_DECODE_FRAME_KEY);
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
            String clStr = f.getReadHeader(Low_Content_Length);
            if (!StringUtil.isNullOrBlank(clStr)) {
                f.contentLength = contentLength = Integer.parseInt(clStr);
            }
            String contentType = f.getReadHeader(Low_Content_Type);
            parseContentType(f, contentType);
            String cookie = f.getReadHeader(Low_Cookie);
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
        int contentLength = f.contentLength;
        int remain = src.remaining();
        if (remain == contentLength) {
            f.bodyArray = src.getBytes();
        } else if (remain < contentLength) {
            setHttpFrame(ch, f);
            return null;
        } else {
            src.markL();
            src.limit(src.position() + contentLength);
            f.bodyArray = src.getBytes();
            src.resetL();
        }
        if (CONTENT_APPLICATION_URLENCODED.equals(f.contentType)) {
            // FIXME encoding
            String paramString = new String(f.bodyArray, ch.getCharset());
            parseParamString(f, paramString);
            f.readText = paramString;
        } else {
            // FIXME 解析BODY中的内容
        }
        doCompplete(ch, f);
        return f;

    }

    private void doCompplete(NioSocketChannel ch, HttpFrame f) {
        ch.removeAttribute(HTTP_DECODE_FRAME_KEY);
    }

    private ByteBuf encode(ByteBufAllocator allocator, HttpFrame f, int length, byte[] array)
            throws IOException {
        ByteBuf buf = allocator.allocate(512);
        try {
            buf.put(PROTOCOL);
            buf.put(f.getStatus().getBinary());
            buf.put(CONTENT_LENGTH);
            buf.put(String.valueOf(length).getBytes());
            buf.putByte(R);
            buf.putByte(N);
            writeHeaders(f.getResponseHeaders(), buf);
            List<Cookie> cookieList = f.getCookieList();
            if (cookieList != null) {
                for (Cookie c : cookieList) {
                    writeBuf(buf, SET_COOKIE);
                    writeBuf(buf, c.toString().getBytes());
                    writeBuf(buf, R);
                    writeBuf(buf, N);
                }
            }
            int len = 2 + length;
            if (buf.remaining() < len) {
                buf.reallocate(buf.position() + len, true);
            }
            buf.putByte(R);
            buf.putByte(N);
            if (length != 0) {
                buf.put(array, 0, length);
            }
        } catch (Exception e) {
            buf.release();
            throw e;
        }
        return buf.flip();
    }

    @Override
    public ByteBuf encode(NioSocketChannel ch, Frame frame) throws IOException {
        ByteBufAllocator allocator = ch.alloc();
        HttpFrame f = (HttpFrame) frame;
        if (f.isUpdateWebSocketProtocol()) {
            ch.setCodec(WebSocketCodec.WS_PROTOCOL_CODEC);
            ch.setAttribute(WebSocketCodec.CHANNEL_KEY_SERVICE_NAME, f.getFrameName());
        }
        f.setResponseHeader(Date_Bytes, getHttpDateBytes());
        byte[] writeBinary = f.getWriteBinary();
        if (writeBinary != null) {
            return encode(ch.alloc(), f, f.getWriteBinarySize(), writeBinary);
        }
        int writeSize = f.getWriteSize();
        if (writeSize == 0) {
            return encode(allocator, f, 0, null);
        }
        return encode(allocator, f, writeSize, f.getWriteBuffer());
    }

    public int getBodyLimit() {
        return bodyLimit;
    }

    public int getHeaderLimit() {
        return headerLimit;
    }

    private byte[] getHttpDateBytes() {
        HDBsHolder h = dateBytes.get();
        if (h == null) {
            h = new HDBsHolder();
            dateBytes.set(h);
        }
        long now = System.currentTimeMillis();
        long time = now % 1000;
        if (time != h.time) {
            h.time = time;
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
        return websocketFrameStackSize;
    }

    public int getWebsocketLimit() {
        return websocketLimit;
    }

    @Override
    public void initialize(ChannelContext context) {
        WebSocketCodec.init(context, websocketLimit, websocketFrameStackSize);
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
        StringLexer l = new StringLexer(0, StringUtil.stringToCharArray(line));
        StringBuilder value = new StringBuilder();
        String k = null;
        String v = null;
        boolean findKey = true;
        for (;;) {
            char c = l.current();
            switch (c) {
                case ' ':
                    break;
                case '=':
                    if (!findKey) {
                        throw new IllegalArgumentException();
                    }
                    k = value.toString();
                    value = new StringBuilder();
                    findKey = false;
                    break;
                case ';':
                    if (findKey) {
                        throw new IllegalArgumentException();
                    }
                    findKey = true;
                    v = value.toString();
                    value = new StringBuilder();
                    cookies.put(k, v);
                    break;
                default:
                    value.append(c);
                    break;
            }
            if (!l.next()) {
                break;
            }
        }
        cookies.put(k, value.toString());
    }

    private void parseContentType(HttpFrame f, String contentType) {
        if (StringUtil.isNullOrBlank(contentType)) {
            f.contentType = CONTENT_APPLICATION_URLENCODED;
            return;
        }
        if (CONTENT_APPLICATION_URLENCODED.equals(contentType)) {
            f.contentType = CONTENT_APPLICATION_URLENCODED;
        } else if (CONTENT_TYPE_TEXT_PLAINUTF8.equals(contentType)) {
            f.contentType = CONTENT_TYPE_TEXT_PLAINUTF8;
        } else if (contentType.startsWith("multipart/form-data;")) {
            int index = KMP_BOUNDARY.match(contentType);
            if (index != -1) {
                f.setBoundary(contentType.substring(index + 9));
            }
            f.contentType = CONTENT_TYPE_MULTIPART;
        } else {
            // FIXME other content-type
            f.contentType = contentType;
        }
    }

    void parseParamString(HttpFrame f, String paramString) {
        boolean findKey = true;
        int lastIndex = 0;
        String key = null;
        String value = null;
        for (int i = 0; i < paramString.length(); i++) {
            if (findKey) {
                if (paramString.charAt(i) == '=') {
                    key = paramString.substring(lastIndex, i);
                    findKey = false;
                    lastIndex = i + 1;
                }
            } else {
                if (paramString.charAt(i) == '&') {
                    value = paramString.substring(lastIndex, i);
                    findKey = true;
                    lastIndex = i + 1;
                    f.params.put(key, value);
                }
            }
        }
        if (lastIndex < paramString.length()) {
            value = paramString.substring(lastIndex);
            f.params.put(key, value);
        }
    }

    private void parseRequestURL(HttpFrame f, int skip, StringBuilder line) {
        int index = line.indexOf("?");
        int lastSpace = StringUtil.lastIndexOf(line, ' ');
        if (index > -1) {
            String paramString = line.substring(index + 1, lastSpace);
            parseParamString(f, paramString);
            f.setRequestURI(line.substring(skip, index));
        } else {
            f.setRequestURI(line.substring(skip, lastSpace));
        }
    }

    void parseFirstLine(HttpFrame f, StringBuilder line) {
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
            currentHeaderLine = getCacheStringBuilder();
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

    private void setHttpFrame(NioSocketChannel ch, HttpFrame f) {
        ch.setAttribute(HTTP_DECODE_FRAME_KEY, f);
    }

    public void setWebsocketFrameStackSize(int websocketFrameStackSize) {
        this.websocketFrameStackSize = websocketFrameStackSize;
    }

    public void setWebsocketLimit(int websocketLimit) {
        this.websocketLimit = websocketLimit;
    }

    void writeBuf(ByteBuf buf, byte b) {
        if (!buf.hasRemaining()) {
            buf.reallocate(buf.capacity() + 1, true);
            buf.limit(buf.capacity());
        }
        buf.putByte(b);
    }

    void writeBuf(ByteBuf buf, byte[] array) {
        writeBuf(buf, array, 0, array.length);
    }

    void writeBuf(ByteBuf buf, byte[] array, int off, int len) {
        if (buf.remaining() < len) {
            buf.reallocate(buf.position() + len, true);
            buf.limit(buf.capacity());
        }
        buf.put(array, off, len);
    }

    private void writeHeaders(Map<byte[], byte[]> headers, ByteBuf buf) {
        if (headers == null) {
            return;
        }
        int len = 0;
        for (Entry<byte[], byte[]> header : headers.entrySet()) {
            byte[] k = header.getKey();
            byte[] v = header.getValue();
            if (v == null) {
                continue;
            }
            len += 4;
            len += k.length;
            len += v.length;
        }
        if (buf.remaining() < len) {
            buf.reallocate(buf.position() + len, true);
        }
        for (Entry<byte[], byte[]> header : headers.entrySet()) {
            byte[] k = header.getKey();
            byte[] v = header.getValue();
            if (v == null) {
                continue;
            }
            buf.put(k);
            buf.putByte(COLON);
            buf.putByte(SPACE);
            buf.put(v);
            buf.putByte(R);
            buf.putByte(N);
        }
    }

    class HDBsHolder {
        long   time;
        byte[] value;
    }

    private static StringBuilder getCacheStringBuilder() {
        StringBuilder cache = stringBuilder.get();
        if (cache == null) {
            cache = new StringBuilder(256);
            stringBuilder.set(cache);
        }
        return cache;
    }

}

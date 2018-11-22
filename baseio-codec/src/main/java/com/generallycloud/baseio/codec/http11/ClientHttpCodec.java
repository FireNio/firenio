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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.ByteBufUtil;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.protocol.Frame;

/**
 * @author wangkai
 *
 */
public class ClientHttpCodec extends HttpCodec {

    private static final byte[] COOKIE                  = "Cookie:".getBytes();
    private static final byte[] PROTOCOL                = " HTTP/1.1\r\nContent-Length: ".getBytes();
    private static final byte   SEMICOLON               = ';';
    private int                 websocketLimit          = 1024 * 128;
    private int                 websocketFrameStackSize = 0;

    @Override
    HttpFrame newHttpFrame(NioSocketChannel ch) {
        return new ClientHttpFrame();
    }

    @Override
    public ByteBuf encode(NioSocketChannel ch, Frame frame) throws IOException {
        ClientHttpFrame f = (ClientHttpFrame) frame;
        int write_size = f.getWriteSize();
        ByteBuf buf = null;
        try {
            byte[] url_bytes = getRequestURI(f).getBytes();
            byte[] method_bytes = f.getMethod().getBytes();
            byte[] length_bytes = String.valueOf(write_size).getBytes();
            int len = method_bytes.length + 1 + url_bytes.length + PROTOCOL.length + length_bytes.length + 2;
            List<byte[]> encode_bytes_array = getEncodeBytesArray();
            int header_size = 0;
            int cookie_size = 0;
            Map<HttpHeader, String> headers = f.getRequestHeaders();
            if (headers != null) {
                headers.remove(HttpHeader.Content_Length);
                for (Entry<HttpHeader, String> header : headers.entrySet()) {
                    byte[] k = header.getKey().getBytes();
                    byte[] v = header.getValue().getBytes();
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
            }
            List<Cookie> cookieList = f.getCookieList();
            if (cookieList != null && !cookieList.isEmpty()) {
                len += COOKIE.length;
                for (Cookie c : cookieList) {
                    byte[] k = c.getName().getBytes();
                    byte[] v = c.getValue().getBytes();
                    cookie_size++;
                    encode_bytes_array.add(k);
                    encode_bytes_array.add(v);
                    len += 2;
                    len += k.length;
                    len += v.length;
                }
            }
            len += 2;
            len += write_size;
            buf = ch.alloc().allocate(len);
            buf.put(method_bytes);
            buf.putByte(SPACE);
            buf.put(url_bytes);
            buf.put(PROTOCOL);
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
                buf.put(encode_bytes_array.get(j++));
                buf.putByte((byte) ':');
                buf.put(encode_bytes_array.get(j++));
                buf.putByte(SEMICOLON);
            }
            buf.putByte(R);
            buf.putByte(N);
            if (write_size != 0) {
                buf.put(f.getWriteBuffer(), 0, write_size);
            }
        } catch (Exception e) {
            buf.release();
            throw e;
        }
        return buf.flip();
    }
    
    @Override
    Frame decodeRemainBody(NioSocketChannel ch,ByteBuf src, HttpFrame frame) {
        ClientHttpFrame f = (ClientHttpFrame) frame;
        if (f.bodyArray == null) {
            f.bodyArray = new byte[f.contentLength];
            f.bodyBuf = ByteBufUtil.wrap(f.bodyArray);
        }
        f.bodyBuf.read(src);
        if (f.bodyBuf.hasRemaining()) {
            setHttpFrame(ch, f);
            return null;
        }
        if (HttpStatic.application_urlencoded.equals(f.contentType)) {
            // FIXME encoding
            String paramString = new String(f.bodyArray, ch.getCharset());
            parse_kv(f.params, paramString, '=', '&');
            f.readText = paramString;
        } else {
            // FIXME 解析BODY中的内容
        }
        doCompplete(ch, f);
        return f;
    }

    protected void parseFirstLine(HttpFrame f, StringBuilder line) {
        int index = StringUtil.indexOf(line, ' ');
        int status = Integer.parseInt(line.substring(index + 1, index + 4));
        f.setVersion(HttpVersion.HTTP1_1);
        f.setStatus(HttpStatus.getStatus(status));
    }

    @Override
    public String getProtocolId() {
        return "HTTP11";
    }

    public int getWebsocketLimit() {
        return websocketLimit;
    }

    public void setWebsocketLimit(int websocketLimit) {
        this.websocketLimit = websocketLimit;
    }

    public int getWebsocketFrameStackSize() {
        return websocketFrameStackSize;
    }

    public void setWebsocketFrameStackSize(int websocketFrameStackSize) {
        this.websocketFrameStackSize = websocketFrameStackSize;
    }

    private String getRequestURI(HttpFrame frame) {
        Map<String, String> params = frame.getRequestParams();
        if (params == null || params.isEmpty()) {
            return frame.getRequestURI();
        }
        String url = frame.getRequestURI();
        StringBuilder u = new StringBuilder(url);
        u.append("?");
        Set<Entry<String, String>> ps = params.entrySet();
        for (Entry<String, String> p : ps) {
            u.append(p.getKey());
            u.append("=");
            u.append(p.getValue());
            u.append("&");
        }
        return u.toString();
    }

    @Override
    public void initialize(ChannelContext context) {
        WebSocketCodec.init(context, websocketLimit, websocketFrameStackSize);
    }

}

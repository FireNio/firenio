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
    private static final byte[] PROTOCOL                = " HTTP/1.1\r\n".getBytes();
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
        byte[] body = f.getRequestBody();
        byte[] urlBytes = getRequestURI(f).getBytes();
        int bodyLen = body == null ? 0 : body.length;
        ByteBuf buf = ch.alloc().allocate(256 + urlBytes.length + bodyLen);
        buf.put(f.getMethod().getBytes());
        buf.putByte(SPACE);
        buf.put(urlBytes);
        buf.put(PROTOCOL);
        writeHeaders(f.getRequestHeaders(), buf);
        List<Cookie> cookieList = f.getCookieList();
        if (cookieList != null && cookieList.size() > 0) {
            buf.put(COOKIE);
            for (Cookie c : cookieList) {
                writeBuf(buf, c.getName().getBytes());
                writeBuf(buf, COLON);
                writeBuf(buf, c.getValue().getBytes());
                writeBuf(buf, SEMICOLON);
            }
            buf.skip(-1);
        }
        buf.putByte(R);
        buf.putByte(N);
        if (body != null) {
            if (buf.remaining() < bodyLen) {
                buf.reallocate(buf.position() + bodyLen, true);
            }
            buf.put(body);
        }
        return buf.flip();
    }

    private void writeHeaders(Map<HttpHeader, String> headers, ByteBuf buf) {
        if (headers == null) {
            return;
        }
        for (Entry<HttpHeader, String> header : headers.entrySet()) {
            if (header.getValue() == null) {
                continue;
            }
            writeBuf(buf, header.getKey().getBytes());
            writeBuf(buf, COLON);
            writeBuf(buf, SPACE);
            writeBuf(buf, header.getValue().getBytes());
            writeBuf(buf, R);
            writeBuf(buf, N);
        }
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
        if (params == null) {
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

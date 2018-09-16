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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.generallycloud.baseio.common.BASE64Util;
import com.generallycloud.baseio.common.Encoding;
import com.generallycloud.baseio.common.SHAUtil;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.NioEventLoop;
import com.generallycloud.baseio.component.NioSocketChannel;
import static com.generallycloud.baseio.codec.http11.HttpStatic.*;
import static com.generallycloud.baseio.codec.http11.HttpHeader.*;

public class ServerHttpFrame extends AbstractHttpFrame {

    private boolean updateWebSocketProtocol;

    public ServerHttpFrame(ChannelContext context, int headerLimit, int bodyLimit) {
        super(bodyLimit, bodyLimit);
        setRequestHeaders(new HashMap<String, String>());
        setRequestParams(new HashMap<String, String>());
        setResponseHeaders(new HashMap<byte[], byte[]>());
        setDefaultResponseHeaders(context, getResponseHeaders());
    }

    public ServerHttpFrame(ChannelContext context) {
        setResponseHeaders(new HashMap<byte[], byte[]>());
        setDefaultResponseHeaders(context, getResponseHeaders());
    }

    private void setDefaultResponseHeaders(ChannelContext context, Map<byte[], byte[]> headers) {
        if (context.getCharset() == Encoding.GBK) {
            headers.put(Content_Type_Bytes, plain_gbk_bytes);
        } else {
            headers.put(Content_Type_Bytes, plain_utf8_bytes);
        }
        headers.put(Server_Bytes, server_baseio_bytes);
        headers.put(Connection_Bytes, keep_alive_bytes); // or close
    }

    @Override
    protected void parseContentType(String contentType) {
        if (StringUtil.isNullOrBlank(contentType)) {
            setContentType(CONTENT_APPLICATION_URLENCODED);
            return;
        }
        if (CONTENT_APPLICATION_URLENCODED.equals(contentType)) {
            setContentType(CONTENT_APPLICATION_URLENCODED);
        } else if (CONTENT_TYPE_TEXT_PLAINUTF8.equals(contentType)) {
            setContentType(CONTENT_TYPE_TEXT_PLAINUTF8);
        } else if (contentType.startsWith("multipart/form-data;")) {
            int index = KMP_BOUNDARY.match(contentType);
            if (index != -1) {
                setBoundary(contentType.substring(index + 9));
            }
            setContentType(CONTENT_TYPE_MULTIPART);
        } else {
            // FIXME other content-type
            setContentType(contentType);
        }
    }

    @Override
    public boolean updateWebSocketProtocol(NioSocketChannel ch) {
        String Sec_WebSocket_Key = getRequestHeader(Low_Sec_WebSocket_Key);
        if (!StringUtil.isNullOrBlank(Sec_WebSocket_Key)) {
            //FIXME 258EAFA5-E914-47DA-95CA-C5AB0DC85B11 必须这个值？
            String Sec_WebSocket_Key_Magic = Sec_WebSocket_Key
                    + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
            byte[] key_array = SHAUtil.SHA1(Sec_WebSocket_Key_Magic);
            String acceptKey = BASE64Util.byteArrayToBase64(key_array);
            setStatus(HttpStatus.C101);
            setResponseHeader(Connection_Bytes, upgrade_bytes);
            setResponseHeader(Upgrade_Bytes, websocket_bytes);
            setResponseHeader(Sec_WebSocket_Accept_Bytes, acceptKey.getBytes());
            updateWebSocketProtocol = true;
            return true;
        }
        return false;
    }

    @Override
    void setReadHeader(String name, String value) {
        setRequestHeader(name, value);
    }

    @Override
    String getReadHeader(String name) {
        return getRequestHeader(name);
    }

    public boolean isUpdateWebSocketProtocol() {
        return updateWebSocketProtocol;
    }

    @Override
    protected void parseFirstLine(StringBuilder line) {
        if (line.charAt(0) == 'G' 
                && line.charAt(1) == 'E' 
                && line.charAt(2) == 'T'
                && line.charAt(3) == ' ') {
            setMethod(HttpMethod.GET);
            parseRequestURL(4, line);
        } else {
            setMethod(HttpMethod.POST);
            parseRequestURL(5, line);
        }
        setVersion(HttpVersion.HTTP1_1);
    }

    protected void parseRequestURL(int skip, StringBuilder line) {
        int index = line.indexOf("?");
        int lastSpace = StringUtil.lastIndexOf(line, ' ');
        if (index > -1) {
            String paramString = line.substring(index + 1, lastSpace);
            parseParamString(paramString);
            setRequestURI(line.substring(skip, index));
        } else {
            setRequestURI(line.substring(skip, lastSpace));
        }
    }

    @SuppressWarnings("unchecked")
    public void release(NioEventLoop eventLoop) {
        //FIXME ..final statck is null or not null
        List<ServerHttpFrame> stack = (List<ServerHttpFrame>) eventLoop
                .getAttribute(ServerHttpCodec.FRAME_STACK_KEY);
        if (stack != null) {
            stack.add(this);
        }
    }

    public ServerHttpFrame reset(NioSocketChannel ch) {
        super.reset();
        this.updateWebSocketProtocol = false;
        this.setDefaultResponseHeaders(ch.getContext(), getResponseHeaders());
        return this;
    }

}

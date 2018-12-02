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

import static com.generallycloud.baseio.codec.http11.HttpHeader.Connection;
import static com.generallycloud.baseio.codec.http11.HttpHeader.Content_Type;
import static com.generallycloud.baseio.codec.http11.HttpHeader.Sec_WebSocket_Accept;
import static com.generallycloud.baseio.codec.http11.HttpHeader.Sec_WebSocket_Key;
import static com.generallycloud.baseio.codec.http11.HttpHeader.Upgrade;
import static com.generallycloud.baseio.codec.http11.HttpStatic.upgrade_bytes;
import static com.generallycloud.baseio.codec.http11.HttpStatic.websocket_bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.common.Assert;
import com.generallycloud.baseio.common.BASE64Util;
import com.generallycloud.baseio.common.SHAUtil;
import com.generallycloud.baseio.common.Util;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.protocol.AbstractFrame;

//FIXME 改进header parser
/**
 * 
 * Content-Type: application/x-www-form-urlencoded</BR> Content-Type:
 * multipart/form-data; boundary=----WebKitFormBoundaryKA6dsRskWA4CdJek
 *
 */
public class HttpFrame extends AbstractFrame implements HttpMessage {

    byte[]                  bodyArray;
    int                     contentLength;
    int                     contentType;
    List<Cookie>            cookieList;
    Map<String, String>     cookies;
    int                     headerLength;
    int                     method;
    Map<String, String>     params           = new HashMap<>();
    int                     decode_state;
    Map<HttpHeader, String> request_headers  = new HashMap<>();
    String                  requestURL;
    Map<HttpHeader, byte[]> response_headers = new HashMap<>();
    HttpStatus              status           = HttpStatus.C200;
    int                     version;

    public void addCookie(Cookie cookie) {
        if (cookieList == null) {
            cookieList = new ArrayList<>();
        }
        cookieList.add(cookie);
    }

    void clear(Collection<?> coll) {
        if (coll == null) {
            return;
        }
        coll.clear();
    }

    void clear(Map<?, ?> map) {
        if (map == null) {
            return;
        }
        map.clear();
    }

    public byte[] getBodyContent() {
        return bodyArray;
    }

    public String getBoundary() {
        if (contentType == HttpContentType.MULTIPART.getId()) {
            return HttpCodec.parseBoundary(getReadHeader(Content_Type));
        }
        return null;
    }

    public int getContentLength() {
        return contentLength;
    }

    public int getContentType() {
        return contentType;
    }

    public String getCookie(String name) {
        if (cookies == null) {
            return null;
        }
        return cookies.get(name);
    }

    public List<Cookie> getCookieList() {
        return cookieList;
    }

    @Override
    public String getFrameName() {
        return getRequestURL();
    }

    public String getHost() {
        return getRequestHeader(HttpHeader.Host);
    }

    public HttpMethod getMethod() {
        return HttpMethod.getMethod(method);
    }

    public int getMethodId() {
        return method;
    }

    String getReadHeader(HttpHeader name) {
        return getRequestHeader(name);
    }

    @Override
    public String getReadText() {
        return null;
    }

    public String getRequestHeader(HttpHeader name) {
        if (name == null) {
            return null;
        }
        return request_headers.get(name);
    }

    public Map<HttpHeader, String> getRequestHeaders() {
        return request_headers;
    }

    public String getRequestParam(String key) {
        return params.get(key);
    }

    public Map<String, String> getRequestParams() {
        return params;
    }

    /**
     * <table summary="Examples of Returned Values">
     * <tr align=left>
     * <th>First line of HTTP request</th>
     * <th>Returned Value</th>
     * <tr>
     * <td>POST /some/path.html HTTP/1.1
     * <td>
     * <td>/some/path.html
     * <tr>
     * <td>GET http://foo.bar/a.html HTTP/1.0
     * <td>
     * <td>/a.html
     * <tr>
     * <td>GET /xyz?a=b HTTP/1.1
     * <td>
     * <td>/xyz
     * </table>
     */
    public String getRequestURL() {
        return requestURL;
    }

    public Map<HttpHeader, byte[]> getResponseHeaders() {
        return response_headers;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public HttpVersion getVersion() {
        return HttpVersion.getMethod(version);
    }

    public int getVersionId() {
        return version;
    }

    public boolean hasBodyContent() {
        return bodyArray != null;
    }

    HttpFrame reset(NioSocketChannel ch) {
        this.bodyArray = null;
        this.contentLength = 0;
        this.contentType = 0;
        this.headerLength = 0;
        this.method = 0;
        this.requestURL = null;
        this.status = HttpStatus.C200;
        this.version = 0;
        this.decode_state = HttpCodec.decode_state_line_one;
        this.clear(cookieList);
        this.clear(cookies);
        this.request_headers.clear();
        this.response_headers.clear();
        this.params.clear();
        super.reset();
        return this;
    }

    public void setMethod(HttpMethod method) {
        this.method = method.getId();
    }

    void setReadHeader(String name, String value) {
        setRequestHeader(name, value);
    }

    public void setRequestHeader(String name, String value) {
        setRequestHeader0(name, value, request_headers);
    }

    void setRequestHeader0(String name, String value, Map<HttpHeader, String> data) {
        if (Util.isNullOrBlank(name)) {
            return;
        }
        HttpHeader header = HttpHeader.ALL.get(name);
        if (header == null) {
            header = HttpHeader.ALL.get(name.toLowerCase());
            if (header == null) {
                return;
            }
        }
        data.put(header, value);
    }

    public void setRequestHeaders(Map<HttpHeader, String> headers) {
        this.request_headers = headers;
    }

    public void setRequestParams(Map<String, String> params) {
        this.params = params;
    }

    void setRequestURI(String requestURI) {
        this.requestURL = requestURI;
    }

    public void setResponseHeader(HttpHeader name, byte[] value) {
        Assert.notNull(name, "null name");
        Assert.notNull(value, "null value");
        response_headers.put(name, value);
    }

    public void setResponseHeaders(Map<HttpHeader, byte[]> headers) {
        this.response_headers = headers;
    }

    public void setReuestParam(String key, String value) {
        if (params == null) {
            params = new HashMap<>();
        }
        this.params.put(key, value);
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return getRequestURL();
    }

    public boolean updateWebSocketProtocol(final NioSocketChannel ch) throws IOException {
        String Sec_WebSocket_Key_Value = getRequestHeader(Sec_WebSocket_Key);
        if (!Util.isNullOrBlank(Sec_WebSocket_Key_Value)) {
            //FIXME 258EAFA5-E914-47DA-95CA-C5AB0DC85B11 必须这个值？
            String Sec_WebSocket_Key_Magic = Sec_WebSocket_Key_Value
                    + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
            byte[] key_array = SHAUtil.SHA1(Sec_WebSocket_Key_Magic);
            String acceptKey = BASE64Util.byteArrayToBase64(key_array);
            setStatus(HttpStatus.C101);
            setResponseHeader(Connection, upgrade_bytes);
            setResponseHeader(Upgrade, websocket_bytes);
            setResponseHeader(Sec_WebSocket_Accept, acceptKey.getBytes());
            ch.setAttribute(WebSocketCodec.CHANNEL_KEY_SERVICE_NAME, getFrameName());
            ch.getEventLoop().execute(new Runnable() {

                @Override
                public void run() {
                    try {
                        ByteBuf buf = ch.encode(HttpFrame.this);
                        ch.setCodec(WebSocketCodec.WS_PROTOCOL_CODEC);
                        ch.flush(buf);
                    } catch (IOException e) {}
                }
            });
            return true;
        }
        return false;
    }

}

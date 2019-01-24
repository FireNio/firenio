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

import static com.firenio.baseio.codec.http11.HttpHeader.Content_Type;
import static com.firenio.baseio.codec.http11.HttpHeader.Sec_WebSocket_Accept;
import static com.firenio.baseio.codec.http11.HttpHeader.Sec_WebSocket_Key;
import static com.firenio.baseio.codec.http11.HttpHeader.Upgrade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.firenio.baseio.buffer.ByteBuf;
import com.firenio.baseio.collection.IntMap;
import com.firenio.baseio.common.Assert;
import com.firenio.baseio.common.Cryptos;
import com.firenio.baseio.common.Util;
import com.firenio.baseio.component.Channel;
import com.firenio.baseio.component.Frame;

//FIXME 改进header parser
/**
 * 
 * Content-Type: application/x-www-form-urlencoded</BR> Content-Type:
 * multipart/form-data; boundary=----WebKitFormBoundaryKA6dsRskWA4CdJek
 *
 */
public class HttpFrame extends Frame {

    private int                 contentLength;
    private int                 connection      = HttpConnection.KEEP_ALIVE.getId();
    private int                 contentType     = HttpContentType.text_plain_utf8.getId();
    private List<Cookie>        cookieList;
    private Map<String, String> cookies;
    private byte[]              date;
    private int                 decodeState;
    private int                 headerLength;
    private boolean             isForm;
    private int                 method;
    private Map<String, String> params          = new HashMap<>();
    private IntMap<String>      request_headers = new IntMap<>(16);
    private String              requestURL;
    private IntMap<byte[]>      response_headers;
    private int                 status          = HttpStatus.C200.getStatus();

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

    void clear(IntMap<byte[]> map) {
        if (map != null) {
            map.clear();
        }
    }

    void clear(Map<?, ?> map) {
        if (map == null) {
            return;
        }
        map.clear();
    }

    public String getBoundary() {
        if (isForm) {
            return HttpCodec.parseBoundary(getRequestHeader(Content_Type.getId()));
        }
        return null;
    }

    public int getContentLength() {
        return contentLength;
    }

    public int getContentTypeId() {
        return contentType;
    }

    public HttpContentType getContentType() {
        return HttpContentType.get(contentType);
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

    public Map<String, String> getCookies() {
        return cookies;
    }

    public byte[] getDate() {
        return date;
    }

    public int getDecodeState() {
        return decodeState;
    }

    @Override
    public String getFrameName() {
        return getRequestURL();
    }

    HttpHeader getHeader(String name) {
        HttpHeader header = HttpHeader.ALL.get(name);
        if (header == null) {
            return HttpHeader.ALL.get(name.toLowerCase());
        }
        return header;
    }

    public int getHeaderLength() {
        return headerLength;
    }

    public String getHost() {
        return getRequestHeader(HttpHeader.Host);
    }

    public HttpMethod getMethod() {
        return HttpMethod.get(method);
    }

    public int getMethodId() {
        return method;
    }

    public String getRequestHeader(HttpHeader name) {
        return request_headers.get(name.getId());
    }

    public String getRequestHeader(int name) {
        return request_headers.get(name);
    }

    public IntMap<String> getRequestHeaders() {
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

    public IntMap<byte[]> getResponseHeaders() {
        return response_headers;
    }

    public HttpStatus getStatus() {
        return HttpStatus.get(status);
    }

    public int getStatusId() {
        return status;
    }

    public boolean hasContent() {
        return getContent() != null;
    }

    @Override
    public int headerLength() {
        return 0;
    }

    public void incrementHeaderLength(int length) {
        this.headerLength += length;
    }

    public boolean isForm() {
        return isForm;
    }

    public boolean isGet() {
        return method == HttpMethod.GET.getId();
    }

    public void removeResponseHeader(HttpHeader header) {
        if (response_headers != null) {
            response_headers.remove(header.getId());
        }
    }

    public HttpFrame reset() {
        this.requestURL = null;
        this.method = 0;
        this.contentLength = 0;
        this.headerLength = 0;
        this.isForm = false;
        this.contentType = HttpContentType.text_plain_utf8.getId();
        this.connection = HttpConnection.KEEP_ALIVE.getId();
        this.status = HttpStatus.C200.getStatus();
        this.decodeState = HttpCodec.decode_state_line_one;
        this.params.clear();
        this.request_headers.clear();
        this.clear(response_headers);
        this.clear(cookieList);
        this.clear(cookies);
        super.reset();
        return this;
    }

    protected void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public void setContentType(HttpContentType contentType) {
        this.contentType = contentType.getId();
    }

    public void setCookies(Map<String, String> cookies) {
        this.cookies = cookies;
    }

    public void setDate(byte[] date) {
        this.date = date;
    }

    protected void setDecodeState(int decodeState) {
        this.decodeState = decodeState;
    }

    public void setForm(boolean isForm) {
        this.isForm = isForm;
    }

    protected void setHeaderLength(int headerLength) {
        this.headerLength = headerLength;
    }

    public void setMethod(HttpMethod method) {
        this.method = method.getId();
    }

    public HttpConnection getConnection() {
        return HttpConnection.get(connection);
    }

    public void setConnection(HttpConnection connection) {
        this.connection = connection.getId();
    }

    public int getConnectionId() {
        return connection;
    }

    void setReadHeader(String name, String value) {
        HttpHeader header = getHeader(name);
        if (header != null) {
            request_headers.put(header.getId(), value);
        }
    }

    public void setRequestHeader(HttpHeader header, String value) {
        this.request_headers.put(header.getId(), value);
    }

    public void setRequestHeaders(IntMap<String> requestHeaders) {
        this.request_headers = requestHeaders;
    }

    public void setRequestParams(Map<String, String> params) {
        this.params = params;
    }

    public void setRequestURL(String url) {
        this.requestURL = url;
    }

    public void setResponseHeader(HttpHeader name, byte[] value) {
        Assert.notNull(name, "null name");
        Assert.notNull(value, "null value");
        if (response_headers == null) {
            response_headers = new IntMap<>(8);
        }
        response_headers.put(name.getId(), value);
    }

    public void setResponseHeader(HttpHeader name, String value) {
        setResponseHeader(name, value.getBytes());
    }

    public void setResponseHeader(int name, byte[] value) {
        Assert.notNull(value, "null value");
        if (response_headers == null) {
            response_headers = new IntMap<>(8);
        }
        response_headers.put(name, value);
    }

    public void setReuestParam(String key, String value) {
        if (params == null) {
            params = new HashMap<>();
        }
        this.params.put(key, value);
    }

    public void setStatus(HttpStatus status) {
        this.status = status.getStatus();
    }

    public String toString() {
        return getRequestURL();
    }

    public boolean updateWebSocketProtocol(final Channel ch) throws Exception {
        String Sec_WebSocket_Key_Value = getRequestHeader(Sec_WebSocket_Key);
        if (!Util.isNullOrBlank(Sec_WebSocket_Key_Value)) {
            //FIXME 258EAFA5-E914-47DA-95CA-C5AB0DC85B11 必须这个值？
            String Sec_WebSocket_Key_Magic = Sec_WebSocket_Key_Value
                    + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
            byte[] key_array = Cryptos.SHA1(Sec_WebSocket_Key_Magic);
            String acceptKey = Cryptos.base64_en(key_array);
            setStatus(HttpStatus.C101);
            setConnection(HttpConnection.UPGRADE);
            setResponseHeader(Upgrade, HttpStatic.websocket_bytes);
            setResponseHeader(Sec_WebSocket_Accept, acceptKey.getBytes());
            ch.setAttribute(WebSocketCodec.CH_KEY_FRAME_NAME, getFrameName());
            ByteBuf buf = ch.encode(this);
            ch.setCodec(WebSocketCodec.PROTOCOL_ID);
            ch.writeAndFlush(buf);
            return true;
        }
        return false;
    }

}

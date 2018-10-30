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

import static com.generallycloud.baseio.codec.http11.HttpHeader.Connection_Bytes;
import static com.generallycloud.baseio.codec.http11.HttpHeader.Content_Type_Bytes;
import static com.generallycloud.baseio.codec.http11.HttpHeader.Low_Sec_WebSocket_Key;
import static com.generallycloud.baseio.codec.http11.HttpHeader.Sec_WebSocket_Accept_Bytes;
import static com.generallycloud.baseio.codec.http11.HttpHeader.Server_Bytes;
import static com.generallycloud.baseio.codec.http11.HttpHeader.Upgrade_Bytes;
import static com.generallycloud.baseio.codec.http11.HttpStatic.keep_alive_bytes;
import static com.generallycloud.baseio.codec.http11.HttpStatic.plain_gbk_bytes;
import static com.generallycloud.baseio.codec.http11.HttpStatic.plain_utf8_bytes;
import static com.generallycloud.baseio.codec.http11.HttpStatic.server_baseio_bytes;
import static com.generallycloud.baseio.codec.http11.HttpStatic.upgrade_bytes;
import static com.generallycloud.baseio.codec.http11.HttpStatic.websocket_bytes;

import java.util.ArrayList;
import java.util.Collection;
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
import com.generallycloud.baseio.protocol.BinaryFrame;

//FIXME 改进header parser
/**
 * 
 * Content-Type: application/x-www-form-urlencoded</BR> Content-Type:
 * multipart/form-data; boundary=----WebKitFormBoundaryKA6dsRskWA4CdJek
 *
 */
public class HttpFrame extends BinaryFrame implements HttpMessage {

    byte[]              bodyArray;
    String              boundary;
    int                 contentLength;
    String              contentType;
    List<Cookie>        cookieList;
    StringBuilder       currentHeaderLine = new StringBuilder();
    boolean             header_complete;
    int                 headerLength;
    HttpMethod          method;
    Map<String, String> params            = new HashMap<>();
    boolean             parseFirstLine    = true;
    String              readText;
    Map<String, String> request_headers   = new HashMap<String, String>();
    String              requestURI;
    Map<byte[], byte[]> response_headers  = new HashMap<byte[], byte[]>();
    HttpStatus          status            = HttpStatus.C200;
    boolean             updateWebSocketProtocol;
    HttpVersion         version;
    Map<String, String> cookies;

    public HttpFrame() {}

    public HttpFrame(ChannelContext context) {
        setDefaultResponseHeaders(context);
    }

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
        return boundary;
    }

    public int getContentLength() {
        return contentLength;
    }

    public String getContentType() {
        return contentType;
    }

    public List<Cookie> getCookieList() {
        return cookieList;
    }

    @Override
    public String getFrameName() {
        return getRequestURI();
    }

    public String getHost() {
        return getRequestHeader(HttpHeader.Low_Host);
    }

    public HttpMethod getMethod() {
        return method;
    }

    String getReadHeader(String name) {
        return getRequestHeader(name);
    }

    @Override
    public String getReadText() {
        return readText;
    }

    public String getRequestHeader(String name) {
        if (StringUtil.isNullOrBlank(name)) {
            return null;
        }
        String _name = HttpHeader.LOW_MAPPING.get(name);
        if (_name == null) {
            _name = name.toLowerCase();
        }
        return request_headers.get(_name);
    }

    public Map<String, String> getRequestHeaders() {
        return request_headers;
    }

    public String getRequestParam(String key) {
        return params.get(key);
    }

    public String getCookie(String name) {
        if (cookies == null) {
            return null;
        }
        return cookies.get(name);
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
    public String getRequestURI() {
        return requestURI;
    }

    public Map<byte[], byte[]> getResponseHeaders() {
        return response_headers;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public HttpVersion getVersion() {
        return version;
    }

    public boolean hasBodyContent() {
        return bodyArray != null;
    }

    public boolean isUpdateWebSocketProtocol() {
        return updateWebSocketProtocol;
    }

    @SuppressWarnings("unchecked")
    public void release(NioEventLoop eventLoop) {
        //FIXME ..final statck is null or not null
        List<HttpFrame> stack = (List<HttpFrame>) eventLoop.getAttribute(HttpCodec.FRAME_STACK_KEY);
        if (stack != null) {
            stack.add(this);
        }
    }

    HttpFrame reset(NioSocketChannel ch) {
        this.bodyArray = null;
        this.boundary = null;
        this.contentLength = 0;
        this.contentType = null;
        this.clear(cookieList);
        this.header_complete = false;
        this.headerLength = 0;
        this.method = null;
        this.parseFirstLine = true;
        this.readText = null;
        this.requestURI = null;
        this.status = HttpStatus.C200;
        this.version = null;
        this.currentHeaderLine.setLength(0);
        this.request_headers.clear();
        this.response_headers.clear();
        this.params.clear();
        this.updateWebSocketProtocol = false;
        this.setDefaultResponseHeaders(ch.getContext());
        super.reset();
        return this;
    }

    void setBoundary(String boundary) {
        this.boundary = boundary;
    }

    void setContentType(String contentType) {
        this.contentType = contentType;
    }

    private void setDefaultResponseHeaders(ChannelContext context) {
        Map<byte[], byte[]> headers = getResponseHeaders();
        if (context.getCharset() == Encoding.GBK) {
            headers.put(Content_Type_Bytes, plain_gbk_bytes);
        } else {
            headers.put(Content_Type_Bytes, plain_utf8_bytes);
        }
        headers.put(Server_Bytes, server_baseio_bytes);
        headers.put(Connection_Bytes, keep_alive_bytes); // or close
    }

    void setMethod(HttpMethod method) {
        this.method = method;
    }

    void setReadHeader(String name, String value) {
        setRequestHeader(name, value);
    }

    public void setRequestHeader(String name, String value) {
        if (StringUtil.isNullOrBlank(name)) {
            return;
        }
        String _name = HttpHeader.LOW_MAPPING.get(name);
        if (_name == null) {
            _name = name.toLowerCase();
        }
        request_headers.put(_name, value);
    }

    public void setRequestHeaders(Map<String, String> headers) {
        this.request_headers = headers;
    }

    public void setRequestParams(Map<String, String> params) {
        this.params = params;
    }

    void setRequestURI(String requestURI) {
        this.requestURI = requestURI;
    }

    public void setResponseHeader(byte[] name, byte[] value) {
        response_headers.put(name, value);
    }

    public void setResponseHeaders(Map<byte[], byte[]> headers) {
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

    void setVersion(HttpVersion version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return getRequestURI();
    }

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

}

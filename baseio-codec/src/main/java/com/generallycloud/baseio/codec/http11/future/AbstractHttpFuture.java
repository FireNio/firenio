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
package com.generallycloud.baseio.codec.http11.future;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.codec.http11.WebSocketProtocolFactory;
import com.generallycloud.baseio.common.BASE64Util;
import com.generallycloud.baseio.common.KMPByteUtil;
import com.generallycloud.baseio.common.KMPUtil;
import com.generallycloud.baseio.common.SHAUtil;
import com.generallycloud.baseio.common.StringLexer;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.ByteArrayBuffer;
import com.generallycloud.baseio.component.MapParameters;
import com.generallycloud.baseio.component.Parameters;
import com.generallycloud.baseio.component.SocketChannel;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.protocol.AbstractChannelFuture;
import com.generallycloud.baseio.protocol.ChannelFuture;
import com.generallycloud.baseio.protocol.ProtocolDecoder;
import com.generallycloud.baseio.protocol.ProtocolEncoder;

//FIXME 改进header parser
/**
 * 
 * Content-Type: application/x-www-form-urlencoded</BR> Content-Type:
 * multipart/form-data; boundary=----WebKitFormBoundaryKA6dsRskWA4CdJek
 *
 */
public abstract class AbstractHttpFuture extends AbstractChannelFuture implements HttpFuture {

    protected static final KMPUtil                  KMP_BOUNDARY        = new KMPUtil("boundary=");
    protected static final KMPByteUtil              KMP_HEADER          = new KMPByteUtil(
            "\r\n\r\n".getBytes());

    protected static final WebSocketProtocolFactory WS_PROTOCOL_FACTORY = new WebSocketProtocolFactory();
    protected static final ProtocolDecoder          WS_PROTOCOL_DECODER = WS_PROTOCOL_FACTORY
            .getProtocolDecoder();
    protected static final ProtocolEncoder          WS_PROTOCOL_ENCODER = WS_PROTOCOL_FACTORY
            .getProtocolEncoder();

    protected ByteArrayBuffer                       binaryBuffer;
    protected boolean                               body_complete;
    protected byte[]                                bodyArray;
    protected int                                   bodyLimit;
    protected String                                boundary;
    protected int                                   contentLength;
    protected String                                contentType;
    protected List<Cookie>                          cookieList;
    protected Map<String, String>                   cookies;
    protected StringBuilder                         currentHeaderLine;
    protected boolean                               hasBodyContent;
    protected boolean                               header_complete;
    protected int                                   headerLength;
    protected int                                   headerLimit;
    protected List<String>                          headerLines;;
    protected String                                host;
    protected String                                method;
    protected Map<String, String>                   params;
    protected Map<String, String>                   request_headers;
    protected String                                requestURI;
    protected String                                requestURL;
    protected Map<String, String>                   response_headers;
    protected SocketChannel                         channel;
    protected HttpStatus                            status              = HttpStatus.C200;
    protected String                                version;

    private MapParameters                           mapParameters;
    private boolean                                 updateWebSocketProtocol;

    public AbstractHttpFuture(SocketChannelContext context) {
        super(context);
    }

    public AbstractHttpFuture(SocketChannel channel, ByteBuf readBuffer, int headerLimit,
            int bodyLimit) {
        super(channel.getContext());
        this.channel = channel;
        this.headerLimit = headerLimit;
        this.bodyLimit = bodyLimit;
        this.cookies = new HashMap<>();
        this.request_headers = new HashMap<>();
        this.headerLines = new ArrayList<>();
        this.currentHeaderLine = new StringBuilder();
    }

    @Override
    public void addCookie(Cookie cookie) {

        if (cookieList == null) {
            cookieList = new ArrayList<>();
        }

        cookieList.add(cookie);
    }

    protected void doBodyComplete() {

        body_complete = true;

        buf.flip();

        bodyArray = buf.getBytes();

        if (CONTENT_APPLICATION_URLENCODED.equals(contentType)) {
            // FIXME encoding

            String paramString = new String(bodyArray, context.getEncoding());

            parseParamString(paramString);

            this.readText = paramString;
        } else {
            // FIXME 解析BODY中的内容
        }
    }

    public void doHeaderComplete(SocketChannel channel, List<String> headerLines) {

        parseFirstLine(headerLines.get(0));

        for (int i = 1; i < headerLines.size(); i++) {

            String l = headerLines.get(i);

            int p = l.indexOf(":");

            if (p == -1) {
                setRequestHeader(l, null);
                continue;
            }

            String name = l.substring(0, p).trim();

            String value = l.substring(p + 1).trim();

            setRequestHeader(name, value);
        }

        host = getRequestHeader("Host");

        String _contentLength = getRequestHeader(HttpHeader.CONTENT_LENGTH);

        int contentLength = 0;

        if (!StringUtil.isNullOrBlank(_contentLength)) {
            contentLength = Integer.parseInt(_contentLength);
            this.contentLength = contentLength;
        }

        String contentType = getRequestHeader(HttpHeader.CONTENT_TYPE);

        parseContentType(contentType);

        String cookie = getRequestHeader("Cookie");

        if (!StringUtil.isNullOrBlank(cookie)) {
            parse_cookies(cookie, cookies);
        }

        if (contentLength < 1) {
            body_complete = true;
            return;
        }

        hasBodyContent = true;

        // FIXME 写入临时文件
        buf = allocate(channel, contentLength, bodyLimit);
    }

    @Override
    public ChannelFuture flush() {

        if (updateWebSocketProtocol) {

            channel.setProtocolDecoder(WS_PROTOCOL_DECODER);
            channel.setProtocolEncoder(WS_PROTOCOL_ENCODER);
            channel.setProtocolFactory(WS_PROTOCOL_FACTORY);

            channel.getSession().setAttribute(WebSocketFuture.SESSION_KEY_SERVICE_NAME, getFutureName());
        }

        return super.flush();
    }

    @Override
    public ByteArrayBuffer getBinaryBuffer() {
        return binaryBuffer;
    }

    @Override
    public byte[] getBodyContent() {
        return bodyArray;
    }

    @Override
    public String getBoundary() {
        return boundary;
    }

    @Override
    public int getContentLength() {
        return contentLength;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public String getCookie(String name) {
        return cookies.get(name);
    }

    @Override
    public List<Cookie> getCookieList() {
        return cookieList;
    }

    @Override
    public String getFutureName() {
        return getRequestURI();
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public Parameters getParameters() {
        if (mapParameters == null) {
            mapParameters = new MapParameters(getRequestParams());
        }
        return mapParameters;
    }

    @Override
    public String getRequestHeader(String name) {

        if (StringUtil.isNullOrBlank(name)) {
            return null;
        }

        //		return request_headers.get(name.toLowerCase());
        return request_headers.get(name);
    }

    @Override
    public Map<String, String> getRequestHeaders() {
        return request_headers;
    }

    @Override
    public String getRequestParam(String key) {
        return params.get(key);
    }

    @Override
    public Map<String, String> getRequestParams() {
        return params;
    }

    @Override
    public String getRequestURI() {
        return requestURI;
    }

    @Override
    public String getRequestURL() {
        return requestURL;
    }

    @Override
    public Map<String, String> getResponseHeaders() {
        if (response_headers == null) {
            response_headers = new HashMap<>();
            setDefaultResponseHeaders(response_headers);
        }
        return response_headers;
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public boolean hasBodyContent() {
        return hasBodyContent;
    }

    private void parse_cookies(String line, Map<String, String> cookies) {
        StringLexer l = new StringLexer(0, line.toCharArray());
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

    protected abstract void parseContentType(String contentType);

    protected abstract void parseFirstLine(String line);

    protected void parseParamString(String paramString) {

        String[] array = paramString.split("&");

        for (String s : array) {

            if (StringUtil.isNullOrBlank(s)) {
                continue;
            }

            String[] unitArray = s.split("=");

            if (unitArray.length != 2) {
                continue;
            }

            String key = unitArray[0];
            String value = unitArray[1];
            params.put(key, value);
        }
    }

    private void readHeader(ByteBuf buffer) throws IOException {

        for (; buffer.hasRemaining();) {

            if (++headerLength > headerLimit) {
                throw new IOException("max http header length " + headerLimit);
            }

            byte b = buffer.getByte();

            if (b == '\n') {
                if (currentHeaderLine.length() == 0) {
                    header_complete = true;
                    break;
                } else {
                    headerLines.add(currentHeaderLine.toString());
                    currentHeaderLine.setLength(0);
                }
                continue;
            } else if (b == '\r') {
                continue;
            } else {
                currentHeaderLine.append((char) b);
            }
        }
    }

    @Override
    public boolean read(SocketChannel channel, ByteBuf buffer) throws IOException {

        if (!header_complete) {

            readHeader(buffer);

            if (!header_complete) {
                return false;
            }

            doHeaderComplete(channel, headerLines);
        }

        if (!body_complete) {

            buf.read(buffer);

            if (buf.hasRemaining()) {
                return false;
            }

            doBodyComplete();
        }

        return true;
    }

    protected abstract void setDefaultResponseHeaders(Map<String, String> headers);

    @Override
    public void setRequestHeader(String name, String value) {

        if (request_headers == null) {
            throw new RuntimeException("did you want to set response header ?");
        }

        if (StringUtil.isNullOrBlank(name)) {
            return;
        }

        //		request_headers.put(name.toLowerCase(), value);
        request_headers.put(name, value);
    }

    @Override
    public void setRequestHeaders(Map<String, String> headers) {
        this.request_headers = headers;
    }

    @Override
    public void setRequestParams(Map<String, String> params) {
        this.params = params;
    }

    @Override
    public void setRequestURL(String url) {
        this.requestURL = url;

        int index = url.indexOf("?");

        if (index > -1) {

            String paramString = url.substring(index + 1, url.length());

            parseParamString(paramString);

            requestURI = url.substring(0, index);

        } else {

            this.requestURI = url;
        }
    }

    @Override
    public void setResponseHeader(String name, String value) {
        if (response_headers == null) {
            response_headers = new HashMap<>();
            setDefaultResponseHeaders(response_headers);
        }
        response_headers.put(name, value);
    }

    @Override
    public void setResponseHeaders(Map<String, String> headers) {
        this.response_headers = headers;
    }

    @Override
    public void setReuestParam(String key, String value) {
        if (params == null) {
            params = new HashMap<>();
        }

        this.params.put(key, value);
    }

    @Override
    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    @Override
    public void updateWebSocketProtocol() {

        String Sec_WebSocket_Key = getRequestHeader("Sec-WebSocket-Key");

        if (!StringUtil.isNullOrBlank(Sec_WebSocket_Key)) {

            //FIXME 258EAFA5-E914-47DA-95CA-C5AB0DC85B11 必须这个值？

            String Sec_WebSocket_Key_Magic = Sec_WebSocket_Key
                    + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

            byte[] key_array = SHAUtil.SHA1(Sec_WebSocket_Key_Magic);

            String acceptKey = BASE64Util.byteArrayToBase64(key_array);

            setStatus(HttpStatus.C101);
            setResponseHeader("Connection", "Upgrade");
            setResponseHeader("Upgrade", "WebSocket");
            setResponseHeader("Sec-WebSocket-Accept", acceptKey);

            updateWebSocketProtocol = true;
            return;
        }
        throw new IllegalArgumentException("illegal http header : empty Sec-WebSocket-Key");
    }

    @Override
    public void writeBinary(byte[] binary) {
        if (binaryBuffer == null) {
            binaryBuffer = new ByteArrayBuffer(binary);
            return;
        }
        binaryBuffer.write(binary);
    }

}

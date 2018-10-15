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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.common.KMPUtil;
import com.generallycloud.baseio.common.StringLexer;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.protocol.BinaryFrame;

//FIXME 改进header parser
/**
 * 
 * Content-Type: application/x-www-form-urlencoded</BR> Content-Type:
 * multipart/form-data; boundary=----WebKitFormBoundaryKA6dsRskWA4CdJek
 *
 */
public abstract class AbstractHttpFrame extends BinaryFrame implements HttpFrame {

    protected static final Map<String, String>      HEADER_LOW_MAPPING = HttpHeader.LOW_MAPPING;
    protected static final KMPUtil                  KMP_BOUNDARY       = new KMPUtil("boundary=");
    private static final ThreadLocal<StringBuilder> stringBuilder      = new ThreadLocal<>();

    private byte[]                                  bodyArray;
    private int                                     bodyLimit;
    private String                                  boundary;
    private int                                     contentLength;
    private String                                  contentType;
    private List<Cookie>                            cookieList;
    private Map<String, String>                     cookies;
    private StringBuilder                           currentHeaderLine;
    private boolean                                 hasBodyContent;
    private boolean                                 header_complete;
    private int                                     headerLength;
    private int                                     headerLimit;
    private String                                  host;
    private HttpMethod                              method;
    private Map<String, String>                     params;
    private boolean                                 parseFirstLine     = true;
    private String                                  readText;
    private Map<String, String>                     request_headers;
    private String                                  requestURI;
    private Map<byte[], byte[]>                    response_headers;
    private HttpStatus                              status             = HttpStatus.C200;
    private HttpVersion                             version;

    AbstractHttpFrame() {}

    public AbstractHttpFrame(int headerLimit, int bodyLimit) {
        this.headerLimit = headerLimit;
        this.bodyLimit = bodyLimit;
    }

    @Override
    public void addCookie(Cookie cookie) {
        if (cookieList == null) {
            cookieList = new ArrayList<>();
        }
        cookieList.add(cookie);
    }

    protected void clear(Collection<?> coll) {
        if (coll == null) {
            return;
        }
        coll.clear();
    }

    protected void clear(Map<?, ?> map) {
        if (map == null) {
            return;
        }
        map.clear();
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
        if (cookies == null) {
            return null;
        }
        return cookies.get(name);
    }

    @Override
    public List<Cookie> getCookieList() {
        return cookieList;
    }

    @Override
    public String getFrameName() {
        return getRequestURI();
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public HttpMethod getMethod() {
        return method;
    }

    abstract String getReadHeader(String name);

    @Override
    public String getReadText() {
        return readText;
    }

    @Override
    public String getRequestHeader(String name) {
        if (StringUtil.isNullOrBlank(name)) {
            return null;
        }
        String _name = HEADER_LOW_MAPPING.get(name);
        if (_name == null) {
            _name = name.toLowerCase();
        }
        return request_headers.get(_name);
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
    public Map<byte[], byte[]> getResponseHeaders() {
        return response_headers;
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public HttpVersion getVersion() {
        return version;
    }

    @Override
    public boolean hasBodyContent() {
        return hasBodyContent;
    }

    private void parse_cookies(String line) {
        if (cookies == null) {
            cookies = new HashMap<>();
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

    protected abstract void parseContentType(String contentType);

    protected abstract void parseFirstLine(StringBuilder line);

    protected void parseParamString(String paramString) {
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
                    params.put(key, value);
                }
            }
        }
        if (lastIndex < paramString.length()) {
            value = paramString.substring(lastIndex);
            params.put(key, value);
        }
    }

    @Override
    public boolean read(NioSocketChannel ch, ByteBuf src) throws IOException {
        if (!header_complete) {
            readHeader(src);
            if (!header_complete) {
                return false;
            }
            host = getReadHeader(HttpHeader.Low_Host);
            String contentLengthStr = getReadHeader(HttpHeader.Low_Content_Length);
            if (!StringUtil.isNullOrBlank(contentLengthStr)) {
                this.contentLength = Integer.parseInt(contentLengthStr);
            }
            String contentType = getReadHeader(HttpHeader.Low_Content_Type);
            parseContentType(contentType);
            String cookie = getReadHeader(HttpHeader.Low_Cookie);
            if (!StringUtil.isNullOrBlank(cookie)) {
                parse_cookies(cookie);
            }
            if (contentLength < 1) {
                return true;
            } else {
                if (contentLength > bodyLimit) {
                    throw new IOException("over limit:" + contentLengthStr);
                }
                hasBodyContent = true;
                // FIXME 写入临时文件
            }
        }
        int remain = src.remaining();
        if (remain == contentLength) {
            bodyArray = src.getBytes();
        } else if (remain < contentLength) {
            return false;
        } else {
            src.markL();
            src.limit(src.position() + contentLength);
            bodyArray = src.getBytes();
            src.resetL();
        }
        if (CONTENT_APPLICATION_URLENCODED.equals(contentType)) {
            // FIXME encoding
            String paramString = new String(bodyArray, ch.getCharset());
            parseParamString(paramString);
            this.readText = paramString;
        } else {
            // FIXME 解析BODY中的内容
        }
        return true;
    }

    private void readHeader(ByteBuf buffer) throws IOException {
        StringBuilder currentHeaderLine = this.currentHeaderLine;
        if (currentHeaderLine == null) {
            currentHeaderLine = getCacheStringBuilder();
        }
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
                    if (parseFirstLine) {
                        parseFirstLine = false;
                        parseFirstLine(currentHeaderLine);
                    } else {
                        int p = StringUtil.indexOf(currentHeaderLine, ':');
                        if (p == -1) {
                            continue;
                        }
                        String name = currentHeaderLine.substring(0, p).trim();
                        String value = currentHeaderLine.substring(p + 1).trim();
                        setReadHeader(name, value);
                    }
                    currentHeaderLine.setLength(0);
                }
                continue;
            } else if (b == '\r') {
                continue;
            } else {
                currentHeaderLine.append((char) b);
            }
        }
        if (!header_complete) {
            if (this.currentHeaderLine == null) {
                this.currentHeaderLine = new StringBuilder(currentHeaderLine.length() + 32);
                this.currentHeaderLine.append(currentHeaderLine);
            }
        }
    }

    protected HttpFrame reset() {
        this.bodyArray = null;
        this.boundary = null;
        this.contentLength = 0;
        this.contentType = null;
        this.clear(cookieList);
        this.clear(cookies);
        this.hasBodyContent = false;
        this.header_complete = false;
        this.headerLength = 0;
        this.host = null;
        this.method = null;
        this.parseFirstLine = true;
        this.readText = null;
        this.requestURI = null;
        this.clear(response_headers);
        this.status = HttpStatus.C200;
        this.version = null;
        if (currentHeaderLine == null) {
            currentHeaderLine = new StringBuilder();
        } else {
            currentHeaderLine.setLength(0);
        }
        if (request_headers == null) {
            request_headers = new HashMap<>();
        } else {
            request_headers.clear();
        }
        if (params == null) {
            params = new HashMap<>();
        } else {
            params.clear();
        }
        super.reset();
        return this;
    }

    protected void setBoundary(String boundary) {
        this.boundary = boundary;
    }

    protected void setContentType(String contentType) {
        this.contentType = contentType;
    }

    protected void setMethod(HttpMethod method) {
        this.method = method;
    }

    abstract void setReadHeader(String name, String value);

    @Override
    public void setRequestHeader(String name, String value) {
        if (StringUtil.isNullOrBlank(name)) {
            return;
        }
        String _name = HEADER_LOW_MAPPING.get(name);
        if (_name == null) {
            _name = name.toLowerCase();
        }
        request_headers.put(_name, value);
    }

    @Override
    public void setRequestHeaders(Map<String, String> headers) {
        this.request_headers = headers;
    }

    @Override
    public void setRequestParams(Map<String, String> params) {
        this.params = params;
    }

    protected void setRequestURI(String requestURI) {
        this.requestURI = requestURI;
    }

    @Override
    public void setResponseHeader(byte[] name, byte[] value) {
        response_headers.put(name, value);
    }

    @Override
    public void setResponseHeaders(Map<byte[], byte[]> headers) {
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

    protected void setVersion(HttpVersion version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return getRequestURI();
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

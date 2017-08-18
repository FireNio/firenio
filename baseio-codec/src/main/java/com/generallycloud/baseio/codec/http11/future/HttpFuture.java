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

import java.util.List;
import java.util.Map;

import com.generallycloud.baseio.component.ByteArrayBuffer;
import com.generallycloud.baseio.protocol.NamedFuture;
import com.generallycloud.baseio.protocol.ParametersFuture;

public abstract interface HttpFuture extends NamedFuture, ParametersFuture {

    public static final String CONTENT_TYPE_MULTIPART           = "multipart/form-data";
    public static final String CONTENT_TYPE_TEXT_PLAIN          = "text/plain";
    public static final String CONTENT_TYPE_TEXT_CSS            = "text/css";
    public static final String CONTENT_TYPE_TEXT_HTML           = "text/html";
    public static final String CONTENT_TYPE_IMAGE_PNG           = "image/png";
    public static final String CONTENT_TYPE_IMAGE_GIF           = "image/gif";
    public static final String CONTENT_TYPE_IMAGE_JPEG          = "image/jpeg";
    public static final String CONTENT_TYPE_IMAGE_ICON          = "image/x-icon";
    public static final String CONTENT_APPLICATION_URLENCODED   = "application/x-www-form-urlencoded";
    public static final String CONTENT_APPLICATION_OCTET_STREAM = "application/octet-stream";
    public static final String CONTENT_APPLICATION_JAVASCRIPT   = "application/x-javascript";

    public abstract String getRequestHeader(String name);

    public abstract void setRequestHeader(String name, String value);

    public abstract void setResponseHeader(String name, String value);

    public abstract Map<String, String> getRequestHeaders();

    public abstract Map<String, String> getResponseHeaders();

    public abstract void setRequestHeaders(Map<String, String> headers);

    public abstract void setResponseHeaders(Map<String, String> headers);

    public abstract String getHost();

    public abstract int getContentLength();

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
    public abstract String getRequestURI();

    public abstract String getRequestURL();

    public abstract void setRequestURL(String url);

    public abstract List<Cookie> getCookieList();

    public abstract String getMethod();

    public abstract String getVersion();

    public abstract String getBoundary();

    public abstract String getContentType();

    public abstract Map<String, String> getRequestParams();

    public abstract String getRequestParam(String key);

    public abstract void setReuestParam(String key, String value);

    public abstract void setRequestParams(Map<String, String> params);

    public abstract byte[] getBodyContent();

    public abstract boolean hasBodyContent();

    public abstract HttpStatus getStatus();

    public abstract void setStatus(HttpStatus status);

    public abstract String getCookie(String name);

    public abstract void addCookie(Cookie cookie);

    public abstract void updateWebSocketProtocol();

    public abstract void writeBinary(byte[] binary);

    public abstract ByteArrayBuffer getBinaryBuffer();
}

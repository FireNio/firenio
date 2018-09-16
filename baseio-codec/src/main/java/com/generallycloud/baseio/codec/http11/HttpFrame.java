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

import java.util.List;
import java.util.Map;

import com.generallycloud.baseio.component.NioSocketChannel;

public interface HttpFrame extends HttpMessage {

    String CONTENT_TYPE_MULTIPART             = "multipart/form-data";
    String CONTENT_TYPE_TEXT_PLAINUTF8        = "text/plain;charset=utf-8";
    String CONTENT_TYPE_TEXT_PLAIN            = "text/plain";
    String CONTENT_TYPE_TEXT_CSSUTF8          = "text/css;charset=utf-8";
    String CONTENT_TYPE_TEXT_HTMLUTF8         = "text/html;charset=utf-8";
    String CONTENT_TYPE_IMAGE_PNG             = "image/png";
    String CONTENT_TYPE_IMAGE_GIF             = "image/gif";
    String CONTENT_TYPE_IMAGE_JPEG            = "image/jpeg";
    String CONTENT_TYPE_IMAGE_ICON            = "image/x-icon";
    String CONTENT_APPLICATION_URLENCODED     = "application/x-www-form-urlencoded";
    String CONTENT_APPLICATION_OCTET_STREAM   = "application/octet-stream";
    String CONTENT_APPLICATION_JAVASCRIPTUTF8 = "application/x-javascript;charset=utf-8";

    String getRequestHeader(String name);

    void setRequestHeader(String name, String value);

    void setResponseHeader(byte[] name, byte[] value);

    Map<String, String> getRequestHeaders();

    Map<byte[], byte[]> getResponseHeaders();

    void setRequestHeaders(Map<String, String> headers);

    void setResponseHeaders(Map<byte[], byte[]> headers);

    String getHost();

    int getContentLength();

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
    String getRequestURI();

    List<Cookie> getCookieList();

    HttpMethod getMethod();

    HttpVersion getVersion();

    String getBoundary();

    String getContentType();

    Map<String, String> getRequestParams();

    String getRequestParam(String key);

    void setReuestParam(String key, String value);

    void setRequestParams(Map<String, String> params);

    byte[] getBodyContent();

    boolean hasBodyContent();

    HttpStatus getStatus();

    void setStatus(HttpStatus status);

    String getCookie(String name);

    void addCookie(Cookie cookie);

    boolean updateWebSocketProtocol(NioSocketChannel ch);

    void writeBinary(byte[] binary);

}

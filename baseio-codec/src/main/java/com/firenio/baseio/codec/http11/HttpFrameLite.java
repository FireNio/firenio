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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.firenio.baseio.collection.IntMap;
import com.firenio.baseio.common.Assert;
import com.firenio.baseio.protocol.AbstractFrame;
import com.firenio.baseio.protocol.NamedFrame;
import com.firenio.baseio.protocol.TextFrame;

//FIXME 改进header parser
/**
 * 
 * Content-Type: application/x-www-form-urlencoded</BR> Content-Type:
 * multipart/form-data; boundary=----WebKitFormBoundaryKA6dsRskWA4CdJek
 *
 */
public class HttpFrameLite extends AbstractFrame implements NamedFrame, TextFrame {

    private byte[]              connection;
    private byte[]              content;
    private int                 contentLength;
    private byte[]              contentType;
    private int                 decodeState;
    private int                 headerLength;
    private int                 method;
    private Map<String, String> params           = new HashMap<>();
    private String              requestURL;
    private IntMap<byte[]>      response_headers;
    private int                 status           = HttpStatus.C200.getStatus();

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

    public byte[] getConnection() {
        return connection;
    }

    public byte[] getContent() {
        return content;
    }

    public int getContentLength() {
        return contentLength;
    }

    public byte[] getContentType() {
        return contentType;
    }

    public int getDecodeState() {
        return decodeState;
    }

    @Override
    public String getFrameName() {
        return getRequestURL();
    }

    public int getHeaderLength() {
        return headerLength;
    }

    public HttpMethod getMethod() {
        return HttpMethod.get(method);
    }

    public int getMethodId() {
        return method;
    }

    @Override
    public String getReadText() {
        return null;
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
        return content != null;
    }

    public void incrementHeaderLength(int length) {
        this.headerLength += length;
    }

    public boolean isGet() {
        return method == HttpMethod.GET.getId();
    }

    public void removeResponseHeader(HttpHeader header){
        if (response_headers != null) {
            response_headers.remove(header.getId());
        }
    }

    public HttpFrameLite reset() {
        this.content = null;
        this.requestURL = null;
        this.method = 0;
        this.contentLength = 0;
        this.headerLength = 0;
        this.status = HttpStatus.C200.getStatus();
        this.decodeState = HttpCodec.decode_state_line_one;
        this.clear(response_headers);
        this.params.clear();
        super.reset();
        return this;
    }

    public void setConnection(byte[] connection) {
        this.connection = connection;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public void setContentType(byte[] contentType) {
        this.contentType = contentType;
    }

    public void setDecodeState(int decodeState) {
        this.decodeState = decodeState;
    }

    public void setHeaderLength(int headerLength) {
        this.headerLength = headerLength;
    }

    public void setMethod(HttpMethod method) {
        this.method = method.getId();
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

}

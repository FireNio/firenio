/*
 * Copyright 2015 The FireNio Project
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
package com.firenio.codec.http11;

import java.io.IOException;

import com.firenio.collection.IntMap;
import com.firenio.common.Util;
import com.firenio.component.Channel;

public class ClientHttpFrame extends HttpFrame {

    boolean        chunked;
    IntMap<String> client_response_headers = new IntMap<>();

    public ClientHttpFrame() {
        this.setMethod(HttpMethod.GET);
    }

    public ClientHttpFrame(String url) {
        this(url, HttpMethod.GET);
    }

    public ClientHttpFrame(String url, HttpMethod method) {
        this.setMethod(method);
        this.setRequestURL(url);
    }

    public String getResponse(HttpHeader header) {
        return getResponse(header.getId());
    }

    public String getResponse(int header) {
        return client_response_headers.get(header);
    }

    public IntMap<String> getResponse_headers() {
        return client_response_headers;
    }

    public boolean isChunked() {
        return chunked || "chunked".equals(getResponse(HttpHeader.Transfer_Encoding));
    }

    @Override
    void setReadHeader(String name, String value) {
        HttpHeader header = getHeader(name);
        if (header != null) {
            client_response_headers.put(header.getId(), value);
        }
    }

    @Override
    public boolean updateWebSocketProtocol(Channel ch) throws IOException {
        String key = getResponse(HttpHeader.Sec_WebSocket_Accept);
        if (Util.isNullOrBlank(key)) {
            return false;
        }
        ch.setCodec(WebSocketCodec.PROTOCOL_ID);
        return true;
    }

}

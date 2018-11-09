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
import java.util.Map;

import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.NioSocketChannel;

public class ClientHttpFrame extends HttpFrame {

    private Map<HttpHeader, String> response_headers = new HashMap<>();

    private byte[]                  requestBody;

    public ClientHttpFrame(String url, HttpMethod method) {
        this.setMethod(method);
        this.setRequestURI(url);
    }

    public ClientHttpFrame(String url) {
        this(url, HttpMethod.GET);
    }

    public ClientHttpFrame() {
        setRequestHeaders(new HashMap<HttpHeader, String>());
    }

    @Override
    public boolean updateWebSocketProtocol(NioSocketChannel ch) {
        String key = getReadHeader(HttpHeader.Sec_WebSocket_Accept);
        if (StringUtil.isNullOrBlank(key)) {
            return false;
        }
        ch.setCodec(WebSocketCodec.WS_PROTOCOL_CODEC);
        return true;
    }

    public byte[] getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(byte[] requestBody) {
        this.requestBody = requestBody;
    }

    @Override
    void setReadHeader(String name, String value) {
        setRequestHeader0(name, value, response_headers);
    }

    @Override
    String getReadHeader(HttpHeader name) {
        return response_headers.get(name);
    }

    public Map<HttpHeader, String> getResponse_headers() {
        return response_headers;
    }

}

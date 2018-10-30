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

    private Map<String, String> response_headers = new HashMap<>();
    
    public ClientHttpFrame(String url, HttpMethod method) {
        this.setMethod(method);
        this.setRequestURI(url);
    }

    public ClientHttpFrame(String url) {
        this(url, HttpMethod.GET);
    }

    public ClientHttpFrame() {
        setRequestHeaders(new HashMap<String, String>());
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

    @Override
    void setReadHeader(String name, String value) {
        if (StringUtil.isNullOrBlank(name)) {
            return;
        }
        String _name = HttpHeader.LOW_MAPPING.get(name);
        if (_name == null) {
            _name = name.toLowerCase();
        }
        response_headers.put(_name, value);
    }

    @Override
    String getReadHeader(String name) {
        if (StringUtil.isNullOrBlank(name)) {
            return null;
        }
        String _name = HttpHeader.LOW_MAPPING.get(name);
        if (_name == null) {
            _name = name.toLowerCase();
        }
        return response_headers.get(_name);
    }

}

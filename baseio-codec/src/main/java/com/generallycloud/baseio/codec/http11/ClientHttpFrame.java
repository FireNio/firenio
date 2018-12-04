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

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.collection.IntMap;
import com.generallycloud.baseio.common.Util;
import com.generallycloud.baseio.component.NioSocketChannel;

public class ClientHttpFrame extends HttpFrame {

    IntMap<String> client_response_headers = new IntMap<>();
    ByteBuf        bodyBuf;

    public ClientHttpFrame(String url, HttpMethod method) {
        this.setMethod(method);
        this.setRequestURI(url);
    }

    public ClientHttpFrame(String url) {
        this(url, HttpMethod.GET);
    }

    public ClientHttpFrame() {
        this.setMethod(HttpMethod.GET);
    }

    @Override
    public boolean updateWebSocketProtocol(final NioSocketChannel ch) {
        String key = getReadHeader(HttpHeader.Sec_WebSocket_Accept);
        if (Util.isNullOrBlank(key)) {
            return false;
        }
        if (ch.inEventLoop()) {
            ch.setCodec(WebSocketCodec.WS_PROTOCOL_CODEC);
        } else {
            ch.getEventLoop().executeAfterLoop(new Runnable() {

                @Override
                public void run() {
                    ch.setCodec(WebSocketCodec.WS_PROTOCOL_CODEC);
                }
            });
        }
        return true;
    }

    @Override
    void setReadHeader(String name, String value) {
        setRequestHeader0(name, value, client_response_headers);
    }

    @Override
    String getReadHeader(HttpHeader name) {
        return client_response_headers.get(name.getId());
    }

    public IntMap<String> getResponse_headers() {
        return client_response_headers;
    }

}

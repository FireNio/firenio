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

import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.NioSocketChannel;

public class ClientHttpFrame extends AbstractHttpFrame {

    public ClientHttpFrame(String url, HttpMethod method) {
        this.setMethod(method);
        this.setRequestURI(url);
        setRequestHeaders(new HashMap<String, String>());
        setRequestParams(new HashMap<String, String>());
    }

    public ClientHttpFrame(String url) {
        this(url, HttpMethod.GET);
    }

    public ClientHttpFrame(int headerLimit, int bodyLimit) {
        super(headerLimit, bodyLimit);
        setRequestHeaders(new HashMap<String, String>());
    }

    @Override
    public boolean updateWebSocketProtocol(NioSocketChannel ch) {
        String key = getRequestHeader(HttpHeader.Sec_WebSocket_Accept);
        if (StringUtil.isNullOrBlank(key)) {
            return false;
        }
        ch.setCodec(WebSocketCodec.WS_PROTOCOL_CODEC);
        return true;
    }

    @Override
    void setReadHeader(String name, String value) {
        setRequestHeader(name, value);
    }

    @Override
    String getReadHeader(String name) {
        return getRequestHeader(name);
    }

    @Override
    protected void parseContentType(String contentType) {
        if (StringUtil.isNullOrBlank(contentType)) {
            setContentType(CONTENT_APPLICATION_URLENCODED);
            return;
        }
        if (CONTENT_APPLICATION_URLENCODED.equals(contentType)) {
            setContentType(CONTENT_APPLICATION_URLENCODED);
        } else if (contentType.startsWith("multipart/form-data;")) {
            int index = KMP_BOUNDARY.match(contentType);
            if (index != -1) {
                setBoundary(contentType.substring(index + 9));
            }
            setContentType(CONTENT_TYPE_MULTIPART);
        } else {
            // FIXME other content-type
        }
    }

    @Override
    protected void parseFirstLine(StringBuilder line) {
        int index = StringUtil.indexOf(line, ' ');
        int status = Integer.parseInt(line.substring(index + 1, index + 4));
        setVersion(HttpVersion.HTTP1_1);
        setStatus(HttpStatus.getStatus(status));
    }

}

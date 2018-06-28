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

public class ClientHttpFuture extends AbstractHttpFuture {

    public ClientHttpFuture(String url, String method) {
        this.setMethod(method);
        this.setRequestURL(url);
        setRequestHeaders(new HashMap<String,String>());
        setRequestParams(new HashMap<String,String>());
    }

    public ClientHttpFuture(String url) {
        this(url, "GET");
    }

    public ClientHttpFuture(int headerLimit, int bodyLimit) {
        super(headerLimit, bodyLimit);
        setResponseHeaders(new HashMap<String,String>());
    }

    @Override
    public boolean updateWebSocketProtocol(NioSocketChannel channel) {
        String key = getResponseHeader(HttpHeader.Sec_WebSocket_Accept);
        if (StringUtil.isNullOrBlank(key)) {
            return false;
        }
        channel.setProtocolCodec(WebSocketCodec.WS_PROTOCOL_CODEC);
        return true;
    }
    
    @Override
    void setReadHeader(String name, String value) {
        setResponseHeader(name, value);
    }

    @Override
    String getReadHeader(String name) {
        return getResponseHeader(name);
    }
    
    @Override
    public void setResponseHeader(String name, String value) {
        if (StringUtil.isNullOrBlank(name)) {
            return;
        }
        String _name = HEADER_LOW_MAPPING.get(name);
        if (_name == null) {
            _name = name.toLowerCase();
        }
        super.setResponseHeader(_name, value);
    }
    
    @Override
    public String getResponseHeader(String name) {
        if (StringUtil.isNullOrBlank(name)) {
            return null;
        }
        String _name = HEADER_LOW_MAPPING.get(name);
        if (_name == null) {
            _name = name.toLowerCase();
        }
        return super.getResponseHeader(_name);
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
    protected void parseFirstLine(String line) {
        int index = line.indexOf(' ');
        int status = Integer.parseInt(line.substring(index + 1, index + 4));
        setVersion(line.substring(0, index));
        setStatus(HttpStatus.getHttpStatus(status));
    }

}

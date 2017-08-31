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

import java.util.Map;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketChannel;

public class ClientHttpFuture extends AbstractHttpFuture {

    public ClientHttpFuture(SocketChannelContext context, String url, String method) {
        super(context);
        this.method = method;
        this.setRequestURL(url);
    }

    public ClientHttpFuture(SocketChannel channel, ByteBuf buffer, int headerLimit, int bodyLimit) {
        super(channel, buffer, headerLimit, bodyLimit);
    }

    @Override
    protected void setDefaultResponseHeaders(Map<String, String> headers) {
        headers.put("Connection", "keep-alive");
    }

    @Override
    public void updateWebSocketProtocol() {
        channel.setProtocolFactory(WS_PROTOCOL_FACTORY);
        channel.setProtocolDecoder(WS_PROTOCOL_DECODER);
        channel.setProtocolEncoder(WS_PROTOCOL_ENCODER);
    }

    @Override
    protected void parseContentType(String contentType) {

        if (StringUtil.isNullOrBlank(contentType)) {

            this.contentType = CONTENT_APPLICATION_URLENCODED;

            return;
        }

        if (CONTENT_APPLICATION_URLENCODED.equals(contentType)) {

            this.contentType = CONTENT_APPLICATION_URLENCODED;

        } else if (contentType.startsWith("multipart/form-data;")) {

            int index = KMP_BOUNDARY.match(contentType);

            if (index != -1) {
                this.boundary = contentType.substring(index + 9);
            }

            this.contentType = CONTENT_TYPE_MULTIPART;
        } else {
            // FIXME other content-type
        }
    }

    @Override
    protected void parseFirstLine(String line) {
        int index = line.indexOf(' ');
        int status = Integer.parseInt(line.substring(index + 1, index + 4));
        this.version = line.substring(0, index);
        this.status = HttpStatus.getHttpStatus(status);
    }

}

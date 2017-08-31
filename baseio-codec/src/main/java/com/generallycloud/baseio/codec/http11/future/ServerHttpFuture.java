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

import java.util.HashMap;
import java.util.Map;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.common.Encoding;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.SocketChannel;
import com.generallycloud.baseio.component.SocketChannelContext;

public class ServerHttpFuture extends AbstractHttpFuture {

    public ServerHttpFuture(SocketChannel channel, ByteBuf buffer, int headerLimit, int bodyLimit) {
        super(channel, buffer, bodyLimit, bodyLimit);
        this.params = new HashMap<>();
    }

    public ServerHttpFuture(SocketChannelContext context) {
        super(context);
    }

    @Override
    protected void setDefaultResponseHeaders(Map<String, String> headers) {

        if (context.getEncoding() == Encoding.GBK) {
            headers.put("Content-Type", "text/plain;charset=gbk");
        } else {
            headers.put("Content-Type", "text/plain;charset=utf-8");
        }
        headers.put("Connection", "keep-alive");
        //		headers.put("Connection", "close");
    }

    @Override
    protected void parseContentType(String contentType) {

        if (StringUtil.isNullOrBlank(contentType)) {

            this.contentType = CONTENT_APPLICATION_URLENCODED;

            return;
        }

        if (CONTENT_APPLICATION_URLENCODED.equals(contentType)) {

            this.contentType = CONTENT_APPLICATION_URLENCODED;

        } else if (CONTENT_TYPE_TEXT_PLAIN.equals(contentType)) {

            this.contentType = CONTENT_TYPE_TEXT_PLAIN;

        } else if (contentType.startsWith("multipart/form-data;")) {

            int index = KMP_BOUNDARY.match(contentType);

            if (index != -1) {
                boundary = contentType.substring(index + 9);
            }

            this.contentType = CONTENT_TYPE_MULTIPART;
        } else {
            // FIXME other content-type
        }
    }

    @Override
    protected void parseFirstLine(String line) {
        int index1 = line.indexOf(' ');
        this.method = line.substring(0, index1);
        int index2 = line.indexOf(' ', index1 + 1);
        this.setRequestURL(line.substring(index1 + 1, index2));
        this.version = line.substring(index2 + 1);
    }

}

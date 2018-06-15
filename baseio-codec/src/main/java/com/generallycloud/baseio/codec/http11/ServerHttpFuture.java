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

import com.generallycloud.baseio.common.Encoding;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.NioSocketChannel;

public class ServerHttpFuture extends AbstractHttpFuture {

    public ServerHttpFuture(NioSocketChannel channel, int headerLimit, int bodyLimit) {
        super(channel, bodyLimit, bodyLimit);
        setRequestParams(new HashMap<String, String>());
    }

    public ServerHttpFuture(ChannelContext context) {
        super(context);
    }

    @Override
    protected void setDefaultResponseHeaders(Map<String, String> headers) {
        if (getContext().getEncoding() == Encoding.GBK) {
            headers.put(HttpHeader.Content_Type, "text/plain;charset=gbk");
        } else {
            headers.put(HttpHeader.Content_Type, "text/plain;charset=utf-8");
        }
        headers.put(HttpHeader.Connection, "keep-alive"); // or close
    }

    @Override
    protected void parseContentType(String contentType) {
        if (StringUtil.isNullOrBlank(contentType)) {
            setContentType(CONTENT_APPLICATION_URLENCODED);
            return;
        }
        if (CONTENT_APPLICATION_URLENCODED.equals(contentType)) {
            setContentType(CONTENT_APPLICATION_URLENCODED);
        } else if (CONTENT_TYPE_TEXT_PLAIN.equals(contentType)) {
            setContentType(CONTENT_TYPE_TEXT_PLAIN);
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
        int index1 = line.indexOf(' ');
        int index2 = line.indexOf(' ', index1 + 1);
        setRequestURL(line.substring(index1 + 1, index2));
        setMethod(line.substring(0, index1));
        setVersion(line.substring(index2 + 1));
    }

//    @Override
//    public void release(NioEventLoop eventLoop) {
//        super.release(eventLoop);
//        //FIXME ..final statck is null or not null
//        FixedThreadStack<ServerHttpFuture> stack = (FixedThreadStack<ServerHttpFuture>) eventLoop
//                .getAttribute(ServerHttpCodec.FUTURE_STACK_KEY);
//        if (stack != null) {
//            stack.push(this);
//        }
//    }

    @Override
    public ServerHttpFuture reset(NioSocketChannel channel, int headerLimit, int bodyLimit) {
        super.reset(channel, headerLimit, bodyLimit);
        setDefaultResponseHeaders(getResponseHeaders());
        return this;
    }

}

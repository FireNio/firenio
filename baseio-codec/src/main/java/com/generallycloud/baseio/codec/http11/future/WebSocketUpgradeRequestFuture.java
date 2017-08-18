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

import com.generallycloud.baseio.common.BASE64Util;
import com.generallycloud.baseio.common.UUIDGenerator;
import com.generallycloud.baseio.component.SocketChannelContext;

public class WebSocketUpgradeRequestFuture extends ClientHttpFuture {

    public WebSocketUpgradeRequestFuture(SocketChannelContext context, String url) {

        super(context, url, "GET");

        this.setResponseHeaders();
    }

    private void setResponseHeaders() {
        setResponseHeader("Connection", "Upgrade");
        setResponseHeader("Upgrade", "websocket");
        setResponseHeader("Sec-WebSocket-Version", "13");
        setResponseHeader("Sec-WebSocket-Key",
                BASE64Util.byteArrayToBase64(UUIDGenerator.random().substring(8, 24).getBytes()));
        setResponseHeader("Sec-WebSocket-Extensions", "permessage-deflate; client_max_window_bits");
    }
}

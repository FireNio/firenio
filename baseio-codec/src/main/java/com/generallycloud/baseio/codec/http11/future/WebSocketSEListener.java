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

import com.generallycloud.baseio.codec.http11.WebSocketProtocolDecoder;
import com.generallycloud.baseio.codec.http11.WebSocketProtocolFactory;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.component.SocketSessionEventListenerAdapter;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

public class WebSocketSEListener extends SocketSessionEventListenerAdapter {

    private Logger logger = LoggerFactory.getLogger(WebSocketSEListener.class);

    @Override
    public void sessionClosed(SocketSession session) {

        if (!WebSocketProtocolFactory.PROTOCOL_ID.equals(session.getProtocolId())) {
            return;
        }

        SocketChannelContext context = session.getContext();

        WebSocketFutureImpl future = new WebSocketFutureImpl(context);

        future.setType(WebSocketProtocolDecoder.TYPE_CLOSE);

        future.setServiceName(session);

        try {
            context.getForeReadFutureAcceptor().accept(session, future);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        super.sessionClosed(session);
    }

}

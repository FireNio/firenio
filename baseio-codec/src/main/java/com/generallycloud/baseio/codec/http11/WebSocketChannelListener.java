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

import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.component.ChannelEventListenerAdapter;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

public class WebSocketChannelListener extends ChannelEventListenerAdapter {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void channelClosed(NioSocketChannel channel) {
        if (!WebSocketCodec.PROTOCOL_ID.equals(channel.getProtocolId())) {
            return;
        }
        ChannelContext context = channel.getContext();
        WebSocketFutureImpl future = new WebSocketFutureImpl();
        future.setType(WebSocketCodec.TYPE_CLOSE);
        future.setServiceName(channel);
        try {
            context.getIoEventHandle().accept(channel, future);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        super.channelClosed(channel);
    }

}

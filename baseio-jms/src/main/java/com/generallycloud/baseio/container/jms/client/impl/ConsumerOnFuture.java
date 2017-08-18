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
package com.generallycloud.baseio.container.jms.client.impl;

import com.generallycloud.baseio.codec.protobase.future.ProtobaseFuture;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.container.OnFuture;
import com.generallycloud.baseio.container.jms.MQException;
import com.generallycloud.baseio.container.jms.Message;
import com.generallycloud.baseio.container.jms.client.OnMessage;
import com.generallycloud.baseio.container.jms.decode.MessageDecoder;
import com.generallycloud.baseio.log.DebugUtil;
import com.generallycloud.baseio.protocol.Future;

public class ConsumerOnFuture implements OnFuture {

    private OnMessage      onMessage;
    private MessageDecoder messageDecoder;

    public ConsumerOnFuture(OnMessage onMessage, MessageDecoder messageDecoder) {
        this.onMessage = onMessage;
        this.messageDecoder = messageDecoder;
    }

    @Override
    public void onResponse(SocketSession session, Future future) {

        ProtobaseFuture f = (ProtobaseFuture) future;

        try {

            Message message = messageDecoder.decode(f);

            onMessage.onReceive(message);

        } catch (MQException e) {
            DebugUtil.debug(e);
        }
    }
}

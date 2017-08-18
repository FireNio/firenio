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
package com.generallycloud.baseio.container.jms.decode;

import com.generallycloud.baseio.codec.protobase.future.ProtobaseFuture;
import com.generallycloud.baseio.container.jms.MQException;
import com.generallycloud.baseio.container.jms.Message;

public class DefaultMessageDecoder implements MessageDecoder {

    private MessageDecoder[] decoders = new MessageDecoder[6];

    public DefaultMessageDecoder() {
        decoders[Message.TYPE_ERROR] = new ErrorMessageDecoder();
        decoders[Message.TYPE_NULL] = new EmptyMessageDecoder();
        decoders[Message.TYPE_TEXT] = new TextMessageDecoder();
        decoders[Message.TYPE_TEXT_BYTE] = new TextByteMessageDecoder();
        decoders[Message.TYPE_MAP] = new MapMessageDecoder();
        decoders[Message.TYPE_MAP_BYTE] = new MapByteMessageDecoder();
    }

    @Override
    public Message decode(ProtobaseFuture future) throws MQException {
        int msgType = future.getParameters().getIntegerParameter("msgType");
        return decoders[msgType].decode(future);
    }
}

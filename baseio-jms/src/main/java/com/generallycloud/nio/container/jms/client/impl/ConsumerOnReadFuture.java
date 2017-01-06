/*
 * Copyright 2015 GenerallyCloud.com
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
package com.generallycloud.nio.container.jms.client.impl;

import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFuture;
import com.generallycloud.nio.common.DebugUtil;
import com.generallycloud.nio.component.OnReadFuture;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.container.jms.MQException;
import com.generallycloud.nio.container.jms.Message;
import com.generallycloud.nio.container.jms.client.OnMessage;
import com.generallycloud.nio.container.jms.decode.MessageDecoder;
import com.generallycloud.nio.protocol.ReadFuture;

public class ConsumerOnReadFuture implements OnReadFuture {

	private OnMessage		onMessage		;
	private MessageDecoder	messageDecoder	;

	public ConsumerOnReadFuture(OnMessage onMessage, MessageDecoder messageDecoder) {
		this.onMessage = onMessage;
		this.messageDecoder = messageDecoder;
	}

	@Override
	public void onResponse(SocketSession session, ReadFuture future) {
		
		ProtobaseReadFuture f = (ProtobaseReadFuture) future;
		
		try {
			
			Message message = messageDecoder.decode(f);

			onMessage.onReceive(message);

		} catch (MQException e) {
			DebugUtil.debug(e);
		}
	}
}

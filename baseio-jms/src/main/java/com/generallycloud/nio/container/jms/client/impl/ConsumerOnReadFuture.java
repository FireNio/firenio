package com.generallycloud.nio.container.jms.client.impl;

import com.generallycloud.nio.codec.base.future.BaseReadFuture;
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

	public void onResponse(SocketSession session, ReadFuture future) {
		
		BaseReadFuture f = (BaseReadFuture) future;
		
		try {
			
			Message message = messageDecoder.decode(f);

			onMessage.onReceive(message);

		} catch (MQException e) {
			e.printStackTrace();
		}
	}
}

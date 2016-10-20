package com.generallycloud.nio.extend.plugin.jms.client.impl;

import com.generallycloud.nio.codec.nio.future.NIOReadFuture;
import com.generallycloud.nio.component.OnReadFuture;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.extend.plugin.jms.MQException;
import com.generallycloud.nio.extend.plugin.jms.Message;
import com.generallycloud.nio.extend.plugin.jms.client.OnMessage;
import com.generallycloud.nio.extend.plugin.jms.decode.MessageDecoder;
import com.generallycloud.nio.protocol.ReadFuture;

public class ConsumerOnReadFuture implements OnReadFuture {

	private OnMessage		onMessage		;
	private MessageDecoder	messageDecoder	;

	public ConsumerOnReadFuture(OnMessage onMessage, MessageDecoder messageDecoder) {
		this.onMessage = onMessage;
		this.messageDecoder = messageDecoder;
	}

	public void onResponse(Session session, ReadFuture future) {
		
		NIOReadFuture f = (NIOReadFuture) future;
		
		try {
			
			Message message = messageDecoder.decode(f);

			onMessage.onReceive(message);

		} catch (MQException e) {
			e.printStackTrace();
		}
	}
}

package com.gifisan.nio.plugin.jms.client.impl;

import com.gifisan.nio.client.ConnectorSession;
import com.gifisan.nio.client.OnReadFuture;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.plugin.jms.JMSException;
import com.gifisan.nio.plugin.jms.Message;
import com.gifisan.nio.plugin.jms.client.OnMessage;
import com.gifisan.nio.plugin.jms.decode.MessageDecoder;

public class ConsumerOnReadFuture implements OnReadFuture {

	private OnMessage		onMessage		= null;
	private MessageDecoder	messageDecoder	= null;

	public ConsumerOnReadFuture(OnMessage onMessage, MessageDecoder messageDecoder) {
		this.onMessage = onMessage;
		this.messageDecoder = messageDecoder;
	}

	public void onResponse(ConnectorSession session, ReadFuture future) {
		try {

			Message message = messageDecoder.decode(future);

			onMessage.onReceive(message);

		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
}

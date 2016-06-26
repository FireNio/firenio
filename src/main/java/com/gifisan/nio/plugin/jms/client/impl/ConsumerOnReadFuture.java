package com.gifisan.nio.plugin.jms.client.impl;

import java.io.OutputStream;

import com.gifisan.nio.component.BufferedOutputStream;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.connector.OnReadFuture;
import com.gifisan.nio.extend.FixedSession;
import com.gifisan.nio.plugin.jms.JMSException;
import com.gifisan.nio.plugin.jms.Message;
import com.gifisan.nio.plugin.jms.client.OnMessage;
import com.gifisan.nio.plugin.jms.decode.MessageDecoder;

public class ConsumerOnReadFuture implements OnReadFuture {

	private OnMessage		onMessage		;
	private MessageDecoder	messageDecoder	;

	public ConsumerOnReadFuture(OnMessage onMessage, MessageDecoder messageDecoder) {
		this.onMessage = onMessage;
		this.messageDecoder = messageDecoder;
	}

	public void onResponse(FixedSession session, ReadFuture future) {
		try {
			
			if (future.hasOutputStream()) {

				OutputStream outputStream = future.getOutputStream();

				if (outputStream == null) {
					future.setOutputStream(new BufferedOutputStream(future.getStreamLength()));
					return;
				}
			}

			Message message = messageDecoder.decode(future);

			onMessage.onReceive(message);

		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
}

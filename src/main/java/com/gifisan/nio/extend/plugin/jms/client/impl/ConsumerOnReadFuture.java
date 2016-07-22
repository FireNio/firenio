package com.gifisan.nio.extend.plugin.jms.client.impl;

import java.io.OutputStream;

import com.gifisan.nio.component.BufferedOutputStream;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.future.ReadFuture;
import com.gifisan.nio.component.protocol.nio.future.NIOReadFuture;
import com.gifisan.nio.extend.OnReadFuture;
import com.gifisan.nio.extend.plugin.jms.MQException;
import com.gifisan.nio.extend.plugin.jms.Message;
import com.gifisan.nio.extend.plugin.jms.client.OnMessage;
import com.gifisan.nio.extend.plugin.jms.decode.MessageDecoder;

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
			
			if (f.hasOutputStream()) {

				OutputStream outputStream = f.getOutputStream();

				if (outputStream == null) {
					f.setOutputStream(new BufferedOutputStream(f.getStreamLength()));
					return;
				}
			}

			Message message = messageDecoder.decode(f);

			onMessage.onReceive(message);

		} catch (MQException e) {
			e.printStackTrace();
		}
	}
}

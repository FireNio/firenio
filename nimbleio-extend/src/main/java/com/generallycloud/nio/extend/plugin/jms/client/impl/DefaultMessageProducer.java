package com.generallycloud.nio.extend.plugin.jms.client.impl;

import java.io.IOException;

import com.generallycloud.nio.codec.nio.future.NIOReadFuture;
import com.generallycloud.nio.extend.FixedSession;
import com.generallycloud.nio.extend.plugin.jms.BytedMessage;
import com.generallycloud.nio.extend.plugin.jms.MQException;
import com.generallycloud.nio.extend.plugin.jms.Message;
import com.generallycloud.nio.extend.plugin.jms.client.MessageProducer;
import com.generallycloud.nio.extend.plugin.jms.server.MQProducerServlet;
import com.generallycloud.nio.extend.plugin.jms.server.MQPublishServlet;

public class DefaultMessageProducer implements MessageProducer {

	private FixedSession session = null;
	
	public DefaultMessageProducer(FixedSession session) {
		this.session = session;
	}

	public boolean offer(Message message) throws MQException {
		return offer(message, MQProducerServlet.SERVICE_NAME);
	}
	
	private boolean offer(Message message,String serviceName) throws MQException {
		
		String param = message.toString();

		NIOReadFuture future = null;

		int msgType = message.getMsgType();

		try {

			if (msgType == Message.TYPE_TEXT || msgType == Message.TYPE_MAP) {

				future = session.request(serviceName, param);

			} else if (msgType == Message.TYPE_TEXT_BYTE || msgType == Message.TYPE_MAP_BYTE) {
				
				BytedMessage _message = (BytedMessage) message;
				
				future = session.request(serviceName, param, _message.getByteArray());
				
			} else {
				
				throw new MQException("msgType:" + msgType);
			}
		} catch (IOException e) {
			
			throw new MQException(e.getMessage(), e);
		}
		
		String result = future.getText();

		if (result.length() == 1) {
			return "T".equals(result);
		}
		throw new MQException(result);

	}

	public boolean publish(Message message) throws MQException {

		return offer(message, MQPublishServlet.SERVICE_NAME);
	}

}

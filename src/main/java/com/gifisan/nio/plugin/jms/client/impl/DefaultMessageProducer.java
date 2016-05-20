package com.gifisan.nio.plugin.jms.client.impl;

import java.io.IOException;

import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.component.ByteArrayInputStream;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.plugin.jms.ByteMessage;
import com.gifisan.nio.plugin.jms.JMSException;
import com.gifisan.nio.plugin.jms.Message;
import com.gifisan.nio.plugin.jms.client.MessageProducer;
import com.gifisan.nio.plugin.jms.server.JMSProducerServlet;
import com.gifisan.nio.plugin.jms.server.JMSPublishServlet;

public class DefaultMessageProducer extends DefaultJMSConnecton implements MessageProducer {

	public DefaultMessageProducer(ClientSession session) {
		super(session);
	}

	public boolean offer(Message message) throws JMSException {
		return offer(message, JMSProducerServlet.SERVICE_NAME);
	}
	
	private boolean offer(Message message,String serviceName) throws JMSException {
		
		String param = message.toString();

		ReadFuture future = null;

		int msgType = message.getMsgType();

		try {

			if (msgType == Message.TYPE_TEXT || msgType == Message.TYPE_MAP) {

				future = session.request(serviceName, param);

			} else if (msgType == Message.TYPE_BYTE) {
				
				ByteMessage _message = (ByteMessage) message;
				
				ByteArrayInputStream inputStream = new ByteArrayInputStream(_message.getByteArray());
				
				future = session.request(serviceName, param, inputStream);
				
			} else {
				
				throw new JMSException("msgType:" + msgType);
			}
		} catch (IOException e) {
			
			throw new JMSException(e.getMessage(), e);
		}
		
		String result = future.getText();

		if (result.length() == 1) {
			return "T".equals(result);
		}
		throw new JMSException(result);

	}

	public boolean publish(Message message) throws JMSException {

		return offer(message, JMSPublishServlet.SERVICE_NAME);
	}

}

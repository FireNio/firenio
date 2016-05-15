package com.gifisan.nio.plugin.jms.client.impl;

import java.io.IOException;

import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.component.ByteArrayInputStream;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.plugin.jms.ByteMessage;
import com.gifisan.nio.plugin.jms.JMSException;
import com.gifisan.nio.plugin.jms.Message;
import com.gifisan.nio.plugin.jms.client.MessageProducer;

public class DefaultMessageProducer extends DefaultJMSConnecton implements MessageProducer {

	public DefaultMessageProducer(ClientSession session){
		super(session);
	}

	public boolean offer(Message message) throws JMSException {
		String param = message.toString();

		ReadFuture future = null;

		int msgType = message.getMsgType();

		if (msgType == 2) {
			try {
				future = session.request("JMSProducerServlet", param);
			} catch (IOException e) {
				throw new JMSException(e.getMessage(), e);
			}
		} else if (msgType == 3) {
			ByteMessage _message = (ByteMessage) message;
			ByteArrayInputStream inputStream = new ByteArrayInputStream(_message.getByteArray());
			try {
				future = session.request("JMSProducerServlet", param, inputStream);
			} catch (IOException e) {
				throw new JMSException(e.getMessage(), e);
			}
		} else {
			throw new JMSException("msgType:" + msgType);
		}
		String result = future.getText();

		if (result.length() == 1) {
			return "T".equals(result);
		}
		throw new JMSException(result);

	}

	public boolean publish(Message message) throws JMSException {

		String param = message.toString();

		ReadFuture future = null;

		int msgType = message.getMsgType();

		if (msgType == 2) {
			try {
				future = session.request("JMSPublishServlet", param);
			} catch (IOException e) {
				throw new JMSException(e.getMessage(), e);
			}
		} else if (msgType == 3) {
			ByteMessage _message = (ByteMessage) message;
			ByteArrayInputStream inputStream = new ByteArrayInputStream(_message.getByteArray());
			try {
				future = session.request("JMSPublishServlet", param, inputStream);
			} catch (IOException e) {
				throw new JMSException(e.getMessage(), e);
			}
		} else {
			throw new JMSException("msgType:" + msgType);
		}
		String result = future.getText();

		if (result.length() == 1) {
			return "T".equals(result);
		}
		throw new JMSException(result);
	}

}

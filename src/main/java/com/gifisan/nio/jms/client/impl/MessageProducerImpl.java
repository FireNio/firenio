package com.gifisan.nio.jms.client.impl;

import java.io.IOException;

import com.gifisan.nio.client.ClientResponse;
import com.gifisan.nio.client.ClientSesssion;
import com.gifisan.nio.component.ByteArrayInputStream;
import com.gifisan.nio.jms.ByteMessage;
import com.gifisan.nio.jms.JMSException;
import com.gifisan.nio.jms.Message;
import com.gifisan.nio.jms.client.MessageProducer;

public class MessageProducerImpl extends JMSConnectonImpl implements MessageProducer {

	public MessageProducerImpl(ClientSesssion session) throws JMSException {
		super(session);
	}

	public boolean offer(Message message) throws JMSException {
		String param = message.toString();

		ClientResponse response = null;

		int msgType = message.getMsgType();

		if (msgType == 2) {
			try {
				response = session.request("JMSProducerServlet", param);
			} catch (IOException e) {
				throw new JMSException(e.getMessage(), e);
			}
		} else if (msgType == 3) {
			ByteMessage _message = (ByteMessage) message;
			ByteArrayInputStream inputStream = new ByteArrayInputStream(_message.getByteArray());
			try {
				response = session.request("JMSProducerServlet", param, inputStream);
			} catch (IOException e) {
				throw new JMSException(e.getMessage(), e);
			}
		} else {
			throw new JMSException("msgType:" + msgType);
		}
		String result = response.getText();

		if (result.length() == 1) {
			return "T".equals(result);
		}
		throw new JMSException(result);

	}

	public boolean publish(Message message) throws JMSException {

		String param = message.toString();

		ClientResponse response = null;

		int msgType = message.getMsgType();

		if (msgType == 2) {
			try {
				response = session.request("JMSPublishServlet", param);
			} catch (IOException e) {
				throw new JMSException(e.getMessage(), e);
			}
		} else if (msgType == 3) {
			ByteMessage _message = (ByteMessage) message;
			ByteArrayInputStream inputStream = new ByteArrayInputStream(_message.getByteArray());
			try {
				response = session.request("JMSPublishServlet", param, inputStream);
			} catch (IOException e) {
				throw new JMSException(e.getMessage(), e);
			}
		} else {
			throw new JMSException("msgType:" + msgType);
		}
		String result = response.getText();

		if (result.length() == 1) {
			return "T".equals(result);
		}
		throw new JMSException(result);
	}

}

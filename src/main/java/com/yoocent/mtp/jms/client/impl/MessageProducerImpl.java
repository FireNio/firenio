package com.yoocent.mtp.jms.client.impl;

import java.io.IOException;

import com.yoocent.mtp.client.Response;
import com.yoocent.mtp.jms.JMSException;
import com.yoocent.mtp.jms.Message;
import com.yoocent.mtp.jms.MessageProducer;
import com.yoocent.mtp.jms.server.JMSProducerServlet;

public class MessageProducerImpl extends ConnectonImpl implements MessageProducer{


	public MessageProducerImpl(String url, String sessionID) throws JMSException {
		super(url, sessionID);
	}

	public boolean send(Message message) throws JMSException {
		String param = message.toString();
		
		Response response;
		try {
			response = client.request(JMSProducerServlet.SERVICE_NAME,param , 0);
		} catch (IOException e) {
			throw new JMSException(e.getMessage(),e);
		}
		String result = response.getContent();
		
		if (result.length() == 1) {
			return "T".equals(result);
		}
		throw new JMSException(result);
		
	}

}

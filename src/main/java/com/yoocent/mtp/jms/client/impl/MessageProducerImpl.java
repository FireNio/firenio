package com.yoocent.mtp.jms.client.impl;

import java.io.IOException;

import com.yoocent.mtp.client.Response;
import com.yoocent.mtp.jms.JMSException;
import com.yoocent.mtp.jms.client.Message;
import com.yoocent.mtp.jms.client.MessageProducer;
import com.yoocent.mtp.jms.server.JMSProducerServlet;

public class MessageProducerImpl extends ConnectonImpl implements MessageProducer{


	public MessageProducerImpl(String url, String username, String password,String sessionID) throws JMSException {
		super(url, username, password, sessionID);
	}

	public boolean send(Message message) throws JMSException {
		String param = message.toString();
		
		Response response;
		try {
			response = client.request(JMSProducerServlet.SERVICE_KEY,param , 0);
		} catch (IOException e) {
			throw new JMSException("IO异常",e);
		}
		String result = response.getContent();
		return "T".equals(result);
		
	}

}

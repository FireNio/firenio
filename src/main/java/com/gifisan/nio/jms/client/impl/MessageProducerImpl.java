package com.gifisan.nio.jms.client.impl;

import java.io.IOException;

import com.gifisan.nio.client.ClientSesssion;
import com.gifisan.nio.client.Response;
import com.gifisan.nio.jms.JMSException;
import com.gifisan.nio.jms.Message;
import com.gifisan.nio.jms.client.MessageProducer;

public class MessageProducerImpl extends ConnectonImpl implements MessageProducer{

	
	
	public MessageProducerImpl(ClientSesssion session) throws JMSException {
		super(session);
	}

	public boolean offer(Message message) throws JMSException {
		String param = message.toString();
		
		Response response;
		try {
			response = session.request("JMSProducerServlet",param);
		} catch (IOException e) {
			throw new JMSException(e.getMessage(),e);
		}
		String result = response.getContent();
		
		if (result.length() == 1) {
			return "T".equals(result);
		}
		throw new JMSException(result);
		
	}
	
	public boolean publish(Message message) throws JMSException{
		
		
		
		
		throw new JMSException("");
	}

}

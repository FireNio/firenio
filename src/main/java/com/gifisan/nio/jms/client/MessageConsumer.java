package com.gifisan.nio.jms.client;

import com.gifisan.nio.jms.JMSException;
import com.gifisan.nio.jms.Message;
import com.gifisan.nio.jms.Transaction;

public interface MessageConsumer extends Transaction , JMSConnection{
	
	public Message receive() throws JMSException;
	
	public Message subscribe() throws JMSException;

}

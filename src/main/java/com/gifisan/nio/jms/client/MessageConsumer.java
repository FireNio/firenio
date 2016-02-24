package com.gifisan.nio.jms.client;

import com.gifisan.nio.jms.JMSException;
import com.gifisan.nio.jms.Message;
import com.gifisan.nio.jms.Transaction;

public interface MessageConsumer extends Transaction , JMSConnection{
	
	public Message revice() throws JMSException;

	
	public Message subscibe() throws JMSException;

	
	
	
}

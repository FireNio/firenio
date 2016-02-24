package com.gifisan.nio.jms.client;

import com.gifisan.nio.jms.JMSException;
import com.gifisan.nio.jms.Message;

public interface MessageProducer extends JMSConnection{

	
	public boolean offer(Message message) throws JMSException;
	
	public boolean publish(Message message) throws JMSException;
	
}

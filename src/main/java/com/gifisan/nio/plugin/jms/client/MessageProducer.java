package com.gifisan.nio.plugin.jms.client;

import com.gifisan.nio.plugin.jms.JMSException;
import com.gifisan.nio.plugin.jms.Message;

public interface MessageProducer{

	public boolean offer(Message message) throws JMSException;
	
	public boolean publish(Message message) throws JMSException;
	
}

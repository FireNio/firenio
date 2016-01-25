package com.gifisan.mtp.jms.client;

import com.gifisan.mtp.jms.JMSException;
import com.gifisan.mtp.jms.Message;

public interface MessageProducer extends JMSConnection{

	
	public boolean offer(Message message) throws JMSException;
	
	public boolean publish(Message message) throws JMSException;
	
}

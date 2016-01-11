package com.gifisan.mtp.jms;

import com.gifisan.mtp.jms.client.JMSConnection;

public interface MessageProducer extends JMSConnection{

	
	public boolean offer(Message message) throws JMSException;
	
	public boolean publish(Message message) throws JMSException;
	
}

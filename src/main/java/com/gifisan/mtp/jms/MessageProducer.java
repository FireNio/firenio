package com.gifisan.mtp.jms;

import com.gifisan.mtp.jms.client.Connection;

public interface MessageProducer extends Connection{

	
	public boolean offer(Message message) throws JMSException;
	
	public boolean publish(Message message) throws JMSException;
	
}

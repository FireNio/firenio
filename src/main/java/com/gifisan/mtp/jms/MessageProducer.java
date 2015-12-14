package com.gifisan.mtp.jms;

import com.gifisan.mtp.jms.client.Connection;

public interface MessageProducer extends Connection{

	
	public boolean send(Message message) throws JMSException;
	
	
	
}

package com.yoocent.mtp.jms;

import com.yoocent.mtp.jms.client.Connection;

public interface MessageProducer extends Connection{

	
	public boolean send(Message message) throws JMSException;
	
	
	
}

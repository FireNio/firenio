package com.yoocent.mtp.jms.client;

import com.yoocent.mtp.jms.JMSException;

public interface MessageProducer extends Connection{

	
	public boolean send(Message message) throws JMSException;
	
	
	
}

package com.gifisan.mtp.jms;

import com.gifisan.mtp.jms.client.JMSConnection;

public interface MessageConsumer extends Transaction , JMSConnection{
	
	public Message revice() throws JMSException;

	
	public Message subscibe() throws JMSException;

	
	
	
}

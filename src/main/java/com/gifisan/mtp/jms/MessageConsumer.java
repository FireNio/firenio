package com.gifisan.mtp.jms;

import com.gifisan.mtp.jms.client.Connection;

public interface MessageConsumer extends Transaction , Connection{
	
	public Message revice() throws JMSException;

	
	public Message subscibe() throws JMSException;

	
	
	
}

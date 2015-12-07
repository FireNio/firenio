package com.yoocent.mtp.jms;

import com.yoocent.mtp.jms.client.Connection;

public interface MessageConsumer extends Transaction , Connection{
	
	public Message revice() throws JMSException;

	
	public Message subscibe() throws JMSException;

	
	
	
}

package com.gifisan.mtp.jms.client;

import com.gifisan.mtp.jms.JMSException;
import com.gifisan.mtp.jms.Message;
import com.gifisan.mtp.jms.Transaction;

public interface MessageConsumer extends Transaction , JMSConnection{
	
	public Message revice() throws JMSException;

	
	public Message subscibe() throws JMSException;

	
	
	
}

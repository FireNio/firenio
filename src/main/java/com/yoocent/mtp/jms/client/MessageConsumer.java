package com.yoocent.mtp.jms.client;

import com.yoocent.mtp.jms.JMSException;
import com.yoocent.mtp.jms.Transaction;

public interface MessageConsumer extends Transaction , Connection{
	
	public Message revice() throws JMSException;

	
	public Message subscibe() throws JMSException;

	
	
	
}

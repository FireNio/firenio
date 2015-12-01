package com.yoocent.mtp.jms.client;

import com.yoocent.mtp.jms.JMSException;

public interface Connection{

	public abstract void connect() throws JMSException;
	
	public abstract String getSessionID();
	
	public abstract void disconnect() ;
}

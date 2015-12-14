package com.gifisan.mtp.jms.client;

import com.gifisan.mtp.jms.JMSException;

public interface Connection{

	public abstract void connect(String username,String password) throws JMSException;
	
	public abstract String getSessionID();
	
	public abstract void disconnect() ;
}

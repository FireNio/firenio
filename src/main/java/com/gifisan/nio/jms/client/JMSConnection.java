package com.gifisan.nio.jms.client;

import com.gifisan.nio.jms.JMSException;

public interface JMSConnection {

	public abstract void login(String username, String password) throws JMSException;

	public abstract void logout();

}

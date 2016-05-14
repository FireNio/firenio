package com.gifisan.nio.plugin.jms.client;

import com.gifisan.nio.plugin.jms.JMSException;

public interface JMSConnection {

	public abstract void login(String username, String password) throws JMSException;

	public abstract void logout();

}

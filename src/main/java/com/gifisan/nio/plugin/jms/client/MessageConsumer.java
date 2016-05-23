package com.gifisan.nio.plugin.jms.client;

import com.gifisan.nio.plugin.jms.JMSException;
import com.gifisan.nio.plugin.jms.Transaction;

public interface MessageConsumer extends Transaction , JMSConnection{
	
	public abstract void receive(OnMessage onMessage) throws JMSException ;
	
	public abstract void subscribe(OnMessage onMessage) throws JMSException ;
}

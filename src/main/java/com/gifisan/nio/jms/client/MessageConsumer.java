package com.gifisan.nio.jms.client;

import com.gifisan.nio.jms.JMSException;
import com.gifisan.nio.jms.Message;
import com.gifisan.nio.jms.Transaction;

public interface MessageConsumer extends Transaction , JMSConnection{
	
	public abstract Message receive() throws JMSException;
	
	public abstract void receive(OnMessage onMessage) throws JMSException ;
	
	public abstract Message subscribe() throws JMSException;

	public abstract void subscribe(OnMessage onMessage) throws JMSException ;
}

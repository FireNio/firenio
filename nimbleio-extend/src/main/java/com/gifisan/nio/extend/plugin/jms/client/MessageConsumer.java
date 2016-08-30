package com.gifisan.nio.extend.plugin.jms.client;

import com.gifisan.nio.extend.plugin.jms.MQException;
import com.gifisan.nio.extend.plugin.jms.Transaction;

public interface MessageConsumer extends Transaction{
	
	public abstract void receive(OnMessage onMessage) throws MQException ;
	
	public abstract void subscribe(OnMessage onMessage) throws MQException ;
}

package com.generallycloud.nio.container.jms.client;

import com.generallycloud.nio.container.jms.MQException;
import com.generallycloud.nio.container.jms.Transaction;

public interface MessageConsumer extends Transaction{
	
	public abstract void receive(OnMessage onMessage) throws MQException ;
	
	public abstract void subscribe(OnMessage onMessage) throws MQException ;
}

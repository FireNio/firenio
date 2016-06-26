package com.gifisan.nio.plugin.jms.client;

import com.gifisan.nio.plugin.jms.MQException;
import com.gifisan.nio.plugin.jms.Message;

public interface MessageProducer{

	public boolean offer(Message message) throws MQException;
	
	public boolean publish(Message message) throws MQException;
	
}

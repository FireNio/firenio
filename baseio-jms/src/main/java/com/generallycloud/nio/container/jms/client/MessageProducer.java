package com.generallycloud.nio.container.jms.client;

import com.generallycloud.nio.container.jms.MQException;
import com.generallycloud.nio.container.jms.Message;

public interface MessageProducer{

	public boolean offer(Message message) throws MQException;
	
	public boolean publish(Message message) throws MQException;
	
}

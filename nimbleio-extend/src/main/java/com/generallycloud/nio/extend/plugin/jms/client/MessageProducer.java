package com.generallycloud.nio.extend.plugin.jms.client;

import com.generallycloud.nio.extend.plugin.jms.MQException;
import com.generallycloud.nio.extend.plugin.jms.Message;

public interface MessageProducer{

	public boolean offer(Message message) throws MQException;
	
	public boolean publish(Message message) throws MQException;
	
}

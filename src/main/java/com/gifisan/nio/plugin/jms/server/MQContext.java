package com.gifisan.nio.plugin.jms.server;

import com.gifisan.nio.component.PluginContext;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.plugin.jms.JMSException;
import com.gifisan.nio.plugin.jms.Message;

public interface MQContext extends PluginContext, MessageQueue {

	public abstract Message browser(String messageID);

	public abstract void consumerMessage(Message message);

	public abstract long getMessageDueTime();
	
	public abstract boolean isOnLine(String queueName);

	public abstract int messageSize();

	public abstract Message parse(ReadFuture future) throws JMSException;

	public abstract void publishMessage(Message message);
	
	public abstract void addReceiver(String queueName);

	public abstract void removeReceiver(String queueName);

	public abstract void setMessageDueTime(long dueTime);
	
	public abstract ConsumerPushHandle getConsumerPushFailedHandle() ;

	public abstract void subscribeMessage(Session session, ReadFuture future, JMSSessionAttachment attachment);
}

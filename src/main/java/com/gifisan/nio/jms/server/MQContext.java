package com.gifisan.nio.jms.server;

import com.gifisan.nio.LifeCycle;
import com.gifisan.nio.component.LoginCenter;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.jms.JMSException;
import com.gifisan.nio.jms.Message;
import com.gifisan.nio.server.IOSession;

public interface MQContext extends MessageQueue, LifeCycle {

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
	
	public abstract void reload();

	public abstract LoginCenter getLoginCenter();
	
	public abstract ConsumerPushFailedHandle getConsumerPushFailedHandle() ;

	public abstract void subscribeMessage(IOSession session, ServerReadFuture future, JMSSessionAttachment attachment);
}

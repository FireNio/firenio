package com.gifisan.nio.jms.server;

import com.gifisan.nio.LifeCycle;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.jms.Message;
import com.gifisan.nio.server.session.IOSession;

public interface MQContext extends MessageQueue, LifeCycle {

	public abstract Message browser(String messageID);

	public abstract void consumerMessage(Message message);

	public abstract long getMessageDueTime();
	
	public abstract boolean isLogined(JMSSessionAttachment attachment);

	public abstract boolean isOnLine(String queueName);

	public abstract boolean login(IOSession session, ReadFuture future, JMSSessionAttachment attachment);

	public abstract int messageSize();

	public abstract Message parse(ReadFuture future);

	public abstract void publishMessage(Message message);

	public abstract void removeReceiver(String queueName);

	public abstract void setMessageDueTime(long dueTime);

	public abstract void setPassword(String password);

	public abstract void setUsername(String username);

	public abstract void subscribeMessage(IOSession session, ServerReadFuture future, JMSSessionAttachment attachment);
}

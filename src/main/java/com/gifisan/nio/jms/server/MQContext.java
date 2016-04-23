package com.gifisan.nio.jms.server;

import com.gifisan.nio.LifeCycle;
import com.gifisan.nio.jms.Message;
import com.gifisan.nio.server.session.ServerSession;
import com.gifisan.nio.server.session.Session;
import com.gifisan.nio.server.session.Session;

public interface MQContext extends Queue, LifeCycle{
	
	public abstract Message browser(String messageID);

	public abstract long getMessageDueTime();
	
	public abstract boolean isLogined(Session session) ;
	
	public abstract int messageSize();
	
	public abstract Message parse(ServerSession session);
	
	public abstract void setLogined(boolean logined,Session session) ;
	
	public abstract void setMessageDueTime(long dueTime);
	
	public abstract void consumerMessage(Message message);

	public abstract void publishMessage(Message message);

	public abstract void subscribeMessage(Session session, JMSSessionAttachment attachment);
	
	public abstract void setUsername(String username);

	public abstract void setPassword(String password);

	public abstract boolean login(ServerSession session, JMSSessionAttachment attachment);

	public abstract boolean isOnLine(String queueName);

	public abstract void removeReceiver(String queueName);
}

package com.gifisan.nio.jms.server;

import com.gifisan.nio.LifeCycle;
import com.gifisan.nio.jms.Message;
import com.gifisan.nio.server.Request;
import com.gifisan.nio.server.Response;
import com.gifisan.nio.server.session.Session;

public interface MQContext extends Queue, LifeCycle{
	
	public abstract Message browser(String messageID);

	public abstract long getMessageDueTime();
	
	public abstract boolean isLogined(Session session) ;
	
	public abstract int messageSize();
	
	public abstract Message parse(Request request);
	
	public abstract void removeConsumer(Consumer consumer);
	
	public abstract void setLogined(boolean logined,Session session) ;
	
	public abstract void setMessageDueTime(long dueTime);
	
	public abstract void consumerMessage(Message message);

	public abstract void removeSubscribe(Consumer consumer);
	
	public abstract boolean publishMessage(Message message);

	public abstract void subscribeMessage(Request request, Response response, JMSSessionAttachment attachment);
	
}

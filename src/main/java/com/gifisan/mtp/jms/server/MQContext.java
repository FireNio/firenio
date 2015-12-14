package com.gifisan.mtp.jms.server;

import com.gifisan.mtp.LifeCycle;
import com.gifisan.mtp.jms.Message;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.session.Session;

public interface MQContext extends MessageQueue, LifeCycle{
	
	public abstract Message browser(String messageID);

	public abstract long getMessageDueTime();
	
	public abstract void setMessageDueTime(long dueTime);
	
	public abstract boolean isLogined(Session session) ;
	
	public abstract Message parse(Request request);
	
	public abstract void setLogined(boolean logined,Session session) ;
	
	public abstract int messageSize();
	
	
}

package com.yoocent.mtp.jms.server;

import com.yoocent.mtp.jms.JMSException;
import com.yoocent.mtp.jms.Message;
import com.yoocent.mtp.server.Request;
import com.yoocent.mtp.server.session.Session;

public interface MQContext{
	
	public abstract boolean isLogined(Session session) ;

	public void setLogined(boolean logined,Session session) ;
	
	public abstract boolean regist(Message message);
	
	public abstract Message pollMessage(Request request,long timeout) throws JMSException;
	
	public Message parse(Request request);
	
	public Message browser(String messageID);
	
}

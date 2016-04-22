package com.gifisan.nio.jms.server;

import com.gifisan.nio.jms.Message;
import com.gifisan.nio.server.session.Session;

public interface Queue {

	public abstract void pollMessage(Session session,JMSSessionAttachment attachment) ;
	
	public abstract void offerMessage(Message message);
	
}

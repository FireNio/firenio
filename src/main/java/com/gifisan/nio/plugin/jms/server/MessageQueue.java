package com.gifisan.nio.plugin.jms.server;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.plugin.jms.Message;

public interface MessageQueue {

	public abstract void pollMessage(Session session,ReadFuture future,JMSSessionAttachment attachment) ;
	
	public abstract void offerMessage(Message message);
	
}

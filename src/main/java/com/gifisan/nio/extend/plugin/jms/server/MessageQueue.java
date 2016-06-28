package com.gifisan.nio.extend.plugin.jms.server;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.extend.plugin.jms.Message;

public interface MessageQueue {

	public abstract void pollMessage(Session session,ReadFuture future,MQSessionAttachment attachment) ;
	
	public abstract void offerMessage(Message message);
	
}

package com.gifisan.nio.extend.plugin.jms.server;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.nio.NIOReadFuture;
import com.gifisan.nio.extend.plugin.jms.Message;

public interface MessageQueue {

	public abstract void pollMessage(Session session,NIOReadFuture future,MQSessionAttachment attachment) ;
	
	public abstract void offerMessage(Message message);
	
}

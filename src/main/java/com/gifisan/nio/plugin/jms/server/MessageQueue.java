package com.gifisan.nio.plugin.jms.server;

import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.plugin.jms.Message;
import com.gifisan.nio.server.IOSession;

public interface MessageQueue {

	public abstract void pollMessage(IOSession session,ServerReadFuture future,JMSSessionAttachment attachment) ;
	
	public abstract void offerMessage(Message message);
	
}

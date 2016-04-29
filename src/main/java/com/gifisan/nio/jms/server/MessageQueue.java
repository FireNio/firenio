package com.gifisan.nio.jms.server;

import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.jms.Message;
import com.gifisan.nio.server.session.IOSession;

public interface MessageQueue {

	public abstract void pollMessage(IOSession session,ServerReadFuture future,JMSSessionAttachment attachment) ;
	
	public abstract void offerMessage(Message message);
	
}

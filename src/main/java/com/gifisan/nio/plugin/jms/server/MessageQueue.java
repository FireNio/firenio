package com.gifisan.nio.plugin.jms.server;

import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.plugin.jms.Message;
import com.gifisan.nio.server.IOSession;

public interface MessageQueue {

	public abstract void pollMessage(IOSession session,ReadFuture future,JMSSessionAttachment attachment) ;
	
	public abstract void offerMessage(Message message);
	
}

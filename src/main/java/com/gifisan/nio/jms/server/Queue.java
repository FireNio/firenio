package com.gifisan.nio.jms.server;

import com.gifisan.nio.component.ReadFuture;
import com.gifisan.nio.jms.Message;
import com.gifisan.nio.server.session.IOSession;

public interface Queue {

	public abstract void pollMessage(IOSession session,ReadFuture future,JMSSessionAttachment attachment) ;
	
	public abstract void offerMessage(Message message);
	
}

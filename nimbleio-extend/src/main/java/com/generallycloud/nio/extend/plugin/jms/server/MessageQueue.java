package com.generallycloud.nio.extend.plugin.jms.server;

import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.protocol.nio.future.NIOReadFuture;
import com.generallycloud.nio.extend.plugin.jms.Message;

public interface MessageQueue {

	public abstract void pollMessage(Session session,NIOReadFuture future,MQSessionAttachment attachment) ;
	
	public abstract void offerMessage(Message message);
	
}

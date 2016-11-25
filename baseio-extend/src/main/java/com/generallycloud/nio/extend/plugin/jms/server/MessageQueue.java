package com.generallycloud.nio.extend.plugin.jms.server;

import com.generallycloud.nio.codec.base.future.BaseReadFuture;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.extend.plugin.jms.Message;

public interface MessageQueue {

	public abstract void pollMessage(SocketSession session,BaseReadFuture future,MQSessionAttachment attachment) ;
	
	public abstract void offerMessage(Message message);
	
}

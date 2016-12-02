package com.generallycloud.nio.container.jms.server;

import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFuture;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.container.jms.Message;

public interface MessageQueue {

	public abstract void pollMessage(SocketSession session,ProtobaseReadFuture future,MQSessionAttachment attachment) ;
	
	public abstract void offerMessage(Message message);
	
}

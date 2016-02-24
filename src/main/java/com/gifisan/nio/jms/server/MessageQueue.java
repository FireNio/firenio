package com.gifisan.nio.jms.server;

import com.gifisan.nio.jms.Message;
import com.gifisan.nio.server.Request;
import com.gifisan.nio.server.Response;

public interface MessageQueue {

	public abstract void pollMessage(Request request,Response response) ;
	
	public abstract boolean offerMessage(Message message);
	
}

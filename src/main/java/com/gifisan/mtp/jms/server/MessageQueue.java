package com.gifisan.mtp.jms.server;

import com.gifisan.mtp.jms.Message;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;

public interface MessageQueue {

	public abstract void pollMessage(Request request,Response response) ;
	
	public abstract boolean offerMessage(Message message);
	
}

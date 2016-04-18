package com.gifisan.nio.jms.server;

import com.gifisan.nio.jms.Message;
import com.gifisan.nio.service.Request;
import com.gifisan.nio.service.Response;

public interface Queue {

	public abstract void pollMessage(Request request,Response response,JMSSessionAttachment attachment) ;
	
	public abstract void offerMessage(Message message);
	
}

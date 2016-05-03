package com.gifisan.nio.jms.server;

import java.io.IOException;

import com.gifisan.nio.component.IOExceptionHandle;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.Future;
import com.gifisan.nio.jms.Message;

public class ConsumerPushFailedHandle implements IOExceptionHandle {

	private MQContext	context	= null;

	public ConsumerPushFailedHandle(MQContext context) {
		this.context = context;
	}

	public void handle(Session session, Future future, IOException e) {
		
		context.offerMessage((Message) future.attachment());
	}

}

package com.gifisan.nio.plugin.jms.server;

import java.io.IOException;

import com.gifisan.nio.component.IOEventHandle;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.Future;

public class ConsumerPushHandle implements IOEventHandle {

	private MQContext	context	= null;

	public ConsumerPushHandle(MQContext context) {
		this.context = context;
	}

	public void handle(Session session, Future future, IOException e) {
		
		Consumer consumer = (Consumer) future.attachment();
		
		context.offerMessage(consumer.getMessage());
	}

	public void handle(Session session, Future future) {
		Consumer consumer = (Consumer) future.attachment();
		
		consumer.refresh();
		
		consumer.getConsumerQueue().offer(consumer);
	}
}

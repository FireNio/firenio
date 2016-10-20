package com.generallycloud.nio.extend.plugin.jms.server;

import com.generallycloud.nio.component.concurrent.ListQueue;
import com.generallycloud.nio.component.concurrent.ListQueueABQ;
import com.generallycloud.nio.extend.plugin.jms.Message;

public class MessageStorage {

	private ListQueue<Message>	messages	= new ListQueueABQ<Message>(1024 * 8 * 10);

	public Message poll(long timeout) {
		return messages.poll(timeout);
	}

	//FIXME offer failed
	public boolean offer(Message message) {

		return messages.offer(message);
	}
	
	public int size(){
		return messages.size();
	}

}
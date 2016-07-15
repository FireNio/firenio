package com.gifisan.nio.extend.plugin.jms.server;

import com.gifisan.nio.component.concurrent.LinkedList;
import com.gifisan.nio.component.concurrent.LinkedListABQ;
import com.gifisan.nio.extend.plugin.jms.Message;

public class MessageStorage {

	private LinkedList<Message>	messages	= new LinkedListABQ<Message>(1024 * 8 * 10);

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
package com.gifisan.nio.extend.plugin.jms.server;

import com.gifisan.nio.component.concurrent.LinkedList;
import com.gifisan.nio.component.concurrent.LinkedListM2O;
import com.gifisan.nio.extend.plugin.jms.Message;

public class MessageStorage {

	private LinkedList<Message>	messages	= new LinkedListM2O<Message>(1024 * 8 * 10);

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
package com.gifisan.nio.plugin.jms.server;

import com.gifisan.nio.concurrent.LinkedList;
import com.gifisan.nio.concurrent.LinkedListM2O;
import com.gifisan.nio.plugin.jms.Message;

public class MessageStorage {

	private LinkedList<Message>	messages	= new LinkedListM2O<Message>(1024 * 8 * 10);

	public Message poll(long timeout) {
		return messages.poll(timeout);
	}

	public void offer(Message message) {

		messages.forceOffer(message);
	}
	
	public int size(){
		return messages.size();
	}

}
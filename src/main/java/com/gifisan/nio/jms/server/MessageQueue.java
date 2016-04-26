package com.gifisan.nio.jms.server;

import com.gifisan.nio.concurrent.LinkedList;
import com.gifisan.nio.concurrent.LinkedListM2O;
import com.gifisan.nio.jms.Message;

public class MessageQueue {

	private LinkedList<Message>	messages	= new LinkedListM2O<Message>();

	public Message poll(long timeout) {
		return messages.poll(timeout);
	}

	public void offer(Message message) {

		messages.forceOffer(message);
	}

}
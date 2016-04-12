package com.gifisan.nio.jms.server;

import com.gifisan.nio.concurrent.LinkedListM2O;
import com.gifisan.nio.jms.Message;

public class MessageQueue {

	// TODO 是否应在此设置多个Queue来分割单个Queue
	private LinkedListM2O<Message>	messages	= new LinkedListM2O<Message>(10240000);

	public Message poll(long timeout) {
		return messages.poll(timeout);
	}

	// TODO offer 返回false进行处理
	public boolean offer(Message message) {

		return this.messages.offer(message);
	}

}
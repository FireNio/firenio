package com.gifisan.nio.jms.server;

import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.concurrent.LinkedListM2O;
import com.gifisan.nio.jms.Message;

public class MessageQueue {

	private LinkedListM2O<Message>	messages	= new LinkedListM2O<Message>();

	public Message poll(long timeout) {
		return messages.poll(timeout);
	}

	public void offer(Message message) {

		if (!messages.offer(message)) {
			
			//FIXME 处理message队列满了的情况
			DebugUtil.info("队列满了=============="+message);
			
		}
	}

}
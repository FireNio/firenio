package com.gifisan.nio.jms.server;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.jms.Message;

public class MessageQueue {
	
	
	//TODO 是否应在此设置多个Queue来分割单个Queue
	private  ArrayBlockingQueue<Message> messages = new ArrayBlockingQueue<Message>(10240000);
	
	public Message poll(long timeout) {
		try {
			return messages.poll(timeout,TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			DebugUtil.debug(e);
		}
		return null;
	}
	
	//TODO offer 返回false进行处理
	public boolean offer(Message message){
		
		return this.messages.offer(message);
	}

}
package com.yoocent.mtp.jms.server;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.yoocent.mtp.jms.client.Message;

public class MessageGroup {
	
	
	//TODO 是否应在此设置多个Queue来分割单个Queue
	private  ArrayBlockingQueue<Message> messages = new ArrayBlockingQueue<Message>(10240000);
	
	public Message poll(long timeout) throws InterruptedException{
		return messages.poll(timeout,TimeUnit.MILLISECONDS);
	}
	
	
	//TODO offer 返回false进行处理
	public boolean offer(Message message){
		
		return this.messages.offer(message);
	}

}
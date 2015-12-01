package com.yoocent.mtp.jms;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MessageGroup {
	
	
	//TODO 是否应在此设置多个Queue来分割单个Queue
	private  ArrayBlockingQueue<JMSMessage> messages = new ArrayBlockingQueue<JMSMessage>(10240000);
	
	public JMSMessage poll(long timeout) throws InterruptedException{
		return messages.poll(timeout,TimeUnit.MILLISECONDS);
	}
	
	
	//TODO offer 返回false进行处理
	public boolean offer(JMSMessage message){
		
		return this.messages.offer(message);
	}

}
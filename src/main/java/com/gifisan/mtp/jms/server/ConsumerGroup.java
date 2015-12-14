package com.gifisan.mtp.jms.server;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ConsumerGroup {
	
	
	//TODO 是否应在此设置多个Queue来分割单个Queue
	private  ArrayBlockingQueue<Consumer> consumers = new ArrayBlockingQueue<Consumer>(10240000);
	
	public Consumer poll(long timeout) {
		try {
			return consumers.poll(timeout,TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Consumer take() {
		try {
			return consumers.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public boolean offer(Consumer consumer){
		
		return this.consumers.offer(consumer);
	}

}
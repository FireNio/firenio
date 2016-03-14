package com.gifisan.nio.jms.server;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.gifisan.nio.common.DebugUtil;

public class ConsumerGroup {

	private ArrayBlockingQueue<Consumer>	consumers	= new ArrayBlockingQueue<Consumer>(128);

	public Consumer poll(long timeout) {
		try {
			return consumers.poll(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			DebugUtil.debug(e);
		}
		return null;
	}

	public int size(){
		return consumers.size();
	}

	public boolean offer(Consumer consumer) {
		return this.consumers.offer(consumer);
	}
	
	public void remove(Consumer consumer) {
		this.consumers.remove(consumer);
	}
}
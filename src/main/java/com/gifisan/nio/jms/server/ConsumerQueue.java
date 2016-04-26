package com.gifisan.nio.jms.server;

import com.gifisan.nio.concurrent.LinkedList;
import com.gifisan.nio.concurrent.LinkedListM2O;

public class ConsumerQueue {

	private LinkedList<Consumer>	consumers	= new LinkedListM2O<Consumer>(128);

	public Consumer poll(long timeout) {
		return consumers.poll(timeout);
	}

	public int size(){
		return consumers.size();
	}

	public void offer(Consumer consumer) {
		this.consumers.forceOffer(consumer);
	}
	
}
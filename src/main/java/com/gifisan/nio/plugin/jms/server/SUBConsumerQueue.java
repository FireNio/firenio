package com.gifisan.nio.plugin.jms.server;

import java.util.List;

import com.gifisan.nio.component.LazyList;

public class SUBConsumerQueue implements ConsumerQueue {

	private LazyList<Consumer> consumers = new LazyList<Consumer>();
	
	public int size(){
		return consumers.size();
	}

	public void offer(Consumer consumer) {
		consumers.add(consumer);
	}

	public void remove(Consumer consumer) {
		consumers.remove(consumer);
	}

	public void remove(List<Consumer> consumers) {
		this.consumers.removeAll(consumers);
	}

	public List<Consumer> snapshot() {
		return consumers.getData();
	}
}
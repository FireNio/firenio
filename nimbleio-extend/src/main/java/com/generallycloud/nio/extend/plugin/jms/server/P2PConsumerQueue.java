package com.generallycloud.nio.extend.plugin.jms.server;

import java.util.List;

import com.generallycloud.nio.component.concurrent.ReentrantList;

public class P2PConsumerQueue implements ConsumerQueue{
	
	private ReentrantList<Consumer> consumers = new ReentrantList<Consumer>();
	
	public int size(){
		return consumers.size();
	}

	public void offer(Consumer consumer) {
		consumers.add(consumer);
	}

	public void remove(Consumer consumer) {
		consumers.remove(consumer);
	}

	public List<Consumer> getSnapshot() {
		return consumers.getSnapshot();
	}
	
	
	
}
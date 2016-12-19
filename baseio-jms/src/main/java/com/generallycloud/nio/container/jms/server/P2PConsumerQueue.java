package com.generallycloud.nio.container.jms.server;

import java.util.List;

import com.generallycloud.nio.component.concurrent.ReentrantList;

public class P2PConsumerQueue implements ConsumerQueue{
	
	private ReentrantList<Consumer> consumers = new ReentrantList<Consumer>();
	
	@Override
	public int size(){
		return consumers.size();
	}

	@Override
	public void offer(Consumer consumer) {
		consumers.add(consumer);
	}

	@Override
	public void remove(Consumer consumer) {
		consumers.remove(consumer);
	}

	@Override
	public List<Consumer> getSnapshot() {
		return consumers.getSnapshot();
	}
	
	
	
}
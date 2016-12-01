package com.generallycloud.nio.container.jms.server;

import java.util.List;

public interface ConsumerQueue {

	public abstract int size();

	public abstract void offer(Consumer consumer);

	public abstract void remove(Consumer consumer);
	
	public abstract List<Consumer> getSnapshot();
	
}
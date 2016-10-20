package com.generallycloud.nio.extend.plugin.jms.server;

import java.util.List;

public interface ConsumerQueue {

	public abstract int size();

	public abstract void offer(Consumer consumer);

	public abstract void remove(Consumer consumer);
	
	public abstract List<Consumer> getSnapshot();
	
}
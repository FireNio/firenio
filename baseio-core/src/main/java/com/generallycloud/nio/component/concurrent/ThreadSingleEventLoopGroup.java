package com.generallycloud.nio.component.concurrent;

public class ThreadSingleEventLoopGroup extends AbstractSingleEventLoopGroup{

	public ThreadSingleEventLoopGroup(String eventLoopName, int eventQueueSize, int eventLoopSize) {
		super(eventLoopName, eventQueueSize, eventLoopSize);
	}

	protected EventLoop newEventLoop(String threadName, int eventQueueSize) {
		return new ThreadSingleEventLoop(threadName, eventQueueSize);
	}
	
}

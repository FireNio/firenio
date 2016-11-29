package com.generallycloud.nio.component.concurrent;

public class ThreadEventLoopGroup extends AbstractEventLoopGroup{

	public ThreadEventLoopGroup(String eventLoopName, int eventQueueSize, int eventLoopSize) {
		super(eventLoopName, eventQueueSize, eventLoopSize);
	}

	protected EventLoop newEventLoop(String threadName, int eventQueueSize) {
		return new ThreadEventLoop(threadName, eventQueueSize);
	}
	
}

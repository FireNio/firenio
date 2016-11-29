package com.generallycloud.nio.component.concurrent;

public class LineSingleEventLoopGroup extends AbstractSingleEventLoopGroup{

	public LineSingleEventLoopGroup(String eventLoopName, int eventQueueSize, int eventLoopSize) {
		super(eventLoopName, eventQueueSize, eventLoopSize);
	}

	protected EventLoop newEventLoop(String threadName, int eventQueueSize) {
		return new LineSingleEventLoop();
	}

}

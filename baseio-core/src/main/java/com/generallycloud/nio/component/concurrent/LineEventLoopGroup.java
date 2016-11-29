package com.generallycloud.nio.component.concurrent;

public class LineEventLoopGroup extends AbstractEventLoopGroup{

	public LineEventLoopGroup(String eventLoopName, int eventQueueSize, int eventLoopSize) {
		super(eventLoopName, eventQueueSize, eventLoopSize);
	}

	protected EventLoop newEventLoop(String threadName, int eventQueueSize) {
		return new LineEventLoop();
	}

}

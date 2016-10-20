package com.generallycloud.nio.component.concurrent;

import com.generallycloud.nio.AbstractLifeCycle;
import com.generallycloud.nio.common.LifeCycleUtil;

public class ExecutorEventLoopGroup extends AbstractLifeCycle implements EventLoopGroup{
	
	private String eventLoopName;
	
	private int maxEventLoopSize;
	
	private int maxEventQueueSize;
	
	private int eventLoopSize;
	
	private EventLoop eventLoop;
	
	private long keepAliveTime;
	
	public ExecutorEventLoopGroup(String eventLoopName, 
			int maxEventLoopSize, 
			int maxEventQueueSize,
			int eventLoopSize, 
			long keepAliveTime) {
		this.eventLoopName = eventLoopName;
		this.maxEventLoopSize = maxEventLoopSize;
		this.maxEventQueueSize = maxEventQueueSize;
		this.eventLoopSize = eventLoopSize;
		this.keepAliveTime = keepAliveTime;
	}

	public EventLoop getNext() {
		return eventLoop;
	}

	protected void doStart() throws Exception {

		eventLoop = new ExecutorEventLoop(eventLoopName, 
				eventLoopSize, 
				maxEventLoopSize,
				maxEventQueueSize, 
				keepAliveTime);
		
		eventLoop.start();
	}


	protected void doStop() throws Exception {
		LifeCycleUtil.stop(eventLoop);
	}
	
}

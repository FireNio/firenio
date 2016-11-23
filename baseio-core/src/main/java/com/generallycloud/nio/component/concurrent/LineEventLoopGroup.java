package com.generallycloud.nio.component.concurrent;

import com.generallycloud.nio.AbstractLifeCycle;
import com.generallycloud.nio.common.LifeCycleUtil;

public class LineEventLoopGroup extends AbstractLifeCycle implements EventLoopGroup{

	private EventLoop eventLoop = new LineEventLoop();
	
	public EventLoop getNext() {
		return eventLoop;
	}

	protected void doStart() throws Exception {
		LifeCycleUtil.start(eventLoop);
	}

	protected void doStop() throws Exception {
		LifeCycleUtil.stop(eventLoop);
	}

}

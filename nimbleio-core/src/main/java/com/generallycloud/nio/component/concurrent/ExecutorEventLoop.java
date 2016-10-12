package com.generallycloud.nio.component.concurrent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.generallycloud.nio.AbstractLifeCycle;

public class ExecutorEventLoop extends AbstractLifeCycle implements EventLoop{

	private int		eventLoopSize		;
	private int		maxEventLoopSize	;
	private long		keepAliveTime		;
	private String		eventLoopName		;
	private int		maxEventQueueSize	;
	private NamedThreadFactory threadFactory;

	public ExecutorEventLoop(String eventLoopName,
			int eventLoopSize, 
			int maxEventLoopSize, 
			int maxEventQueueSize,
			long keepAliveTime) {
		this.eventLoopSize = eventLoopSize;
		this.maxEventLoopSize = maxEventLoopSize;
		this.maxEventQueueSize = maxEventQueueSize;
		this.keepAliveTime = keepAliveTime;
		this.eventLoopName = eventLoopName;
	}

	private ThreadPoolExecutor	poolExecutor	;

	public void dispatch(Runnable job) {
		this.poolExecutor.execute(job);

	}

	protected void doStart() throws Exception {

		threadFactory = new NamedThreadFactory(eventLoopName);

		poolExecutor = new ThreadPoolExecutor(eventLoopSize, maxEventLoopSize, keepAliveTime, TimeUnit.MILLISECONDS,
				new ArrayBlockingQueue<Runnable>(maxEventQueueSize), threadFactory);
	}

	protected void doStop() throws Exception {
		
		if (poolExecutor != null) {
			poolExecutor.shutdown();
		}
	}

	public boolean inEventLoop(Thread thread) {
		return threadFactory.inFactory(thread);
	}

}

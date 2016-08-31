package com.generallycloud.nio.component.concurrent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.generallycloud.nio.AbstractLifeCycle;

public class ExecutorThreadPool extends AbstractLifeCycle implements ThreadPool{

	private int		corePoolSize		;
	private int		maximumPoolSize	;
	private long		keepAliveTime		;
	private String		threadPoolName		;
	private int		maximumJobSize		;

	public ExecutorThreadPool(String threadPoolName,
			int corePoolSize, 
			int maximumPoolSize, 
			int maximumJobSize,
			long keepAliveTime) {
		this.corePoolSize = corePoolSize;
		this.maximumPoolSize = maximumPoolSize;
		this.maximumJobSize = maximumJobSize;
		this.keepAliveTime = keepAliveTime;
		this.threadPoolName = threadPoolName;
	}

	private ThreadPoolExecutor	poolExecutor	;

	public void dispatch(Runnable job) {
		this.poolExecutor.execute(job);

	}

	protected void doStart() throws Exception {

		NamedThreadFactory threadFactory = new NamedThreadFactory(threadPoolName);

		poolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS,
				new ArrayBlockingQueue<Runnable>(maximumJobSize), threadFactory);
	}

	protected void doStop() throws Exception {
		
		if (poolExecutor != null) {
			poolExecutor.shutdown();
		}
	}

}

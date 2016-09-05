package com.generallycloud.nio.component.concurrent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import com.generallycloud.nio.AbstractLifeCycle;
import com.generallycloud.nio.Looper;
import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;

public class SingleEventLoop extends AbstractLifeCycle implements EventLoop,Looper{
	
	private static Logger logger = LoggerFactory.getLogger(SingleEventLoop.class);

	private UniqueThread thread;
	
	private ArrayBlockingQueue<Runnable> jobs;
	
	public SingleEventLoop(String threadName,int queueSize) {
		this.jobs = new ArrayBlockingQueue<Runnable>(queueSize);
		this.thread = new UniqueThread(this, threadName);
	}

	public void dispatch(Runnable job) {
		
		if(!jobs.offer(job)){
			throw new RejectedExecutionException();
		}
	}

	protected void doStart() throws Exception {
		
		thread.start();
	}

	protected void doStop() throws Exception {
		
		LifeCycleUtil.stop(thread);
	}

	public void loop() {
		
		try {
			
			Runnable runnable = jobs.poll(32, TimeUnit.MILLISECONDS);
			
			if (runnable == null) {
				return;
			}
			
			runnable.run();
		} catch (Throwable e) {
			logger.error(e.getMessage(),e);
		}
	}
	
	public String toString() {
		return thread.toString();
	}
}

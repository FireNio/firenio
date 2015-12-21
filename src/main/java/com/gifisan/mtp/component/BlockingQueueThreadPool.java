package com.gifisan.mtp.component;

import com.gifisan.mtp.schedule.Job;

public final class BlockingQueueThreadPool extends ThreadPoolImpl implements ThreadPool{
	
	public BlockingQueueThreadPool(String threadPrefix) {
		super(new ArrayBlockingQueue4PoolWorker<Job>(999999), threadPrefix);
	}
	
	public BlockingQueueThreadPool(String threadPrefix,int size) {
		super(new ArrayBlockingQueue4PoolWorker<Job>(size), threadPrefix);
	}
}
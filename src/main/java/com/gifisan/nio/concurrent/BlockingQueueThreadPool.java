package com.gifisan.nio.concurrent;


public final class BlockingQueueThreadPool extends ThreadPoolImpl implements ThreadPool{
	
	public BlockingQueueThreadPool(String threadPrefix) {
		super(new ABQueue4PoolWorker<Runnable>(999999), threadPrefix);
	}
	
	public BlockingQueueThreadPool(String threadPrefix,int size) {
		super(new ABQueue4PoolWorker<Runnable>(999999), threadPrefix,size);
	}
}
package com.gifisan.nio.concurrent;



public final class LinkNodeQueueThreadPool extends ThreadPoolImpl implements ThreadPool{

	public LinkNodeQueueThreadPool(String threadPrefix) {
		super(new LinkNodeQueue<Runnable>(), threadPrefix);
	}
	
	public LinkNodeQueueThreadPool(String threadPrefix,int size) {
		super(new LinkNodeQueue<Runnable>(), threadPrefix,size);
	}
}
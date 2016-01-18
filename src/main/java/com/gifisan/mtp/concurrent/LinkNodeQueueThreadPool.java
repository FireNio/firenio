package com.gifisan.mtp.concurrent;

import com.gifisan.mtp.schedule.Job;


public final class LinkNodeQueueThreadPool extends ThreadPoolImpl implements ThreadPool{

	public LinkNodeQueueThreadPool(String threadPrefix) {
		super(new LinkNodeQueue<Job>(), threadPrefix);
	}
	
	public LinkNodeQueueThreadPool(String threadPrefix,int size) {
		super(new LinkNodeQueue<Job>(), threadPrefix,size);
	}
}
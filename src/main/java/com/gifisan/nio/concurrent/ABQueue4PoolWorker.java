package com.gifisan.nio.concurrent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.gifisan.nio.common.DebugUtil;

public class ABQueue4PoolWorker<T> implements Queue<T> {

	private java.util.concurrent.ArrayBlockingQueue<T> queue = null;
	
	public ABQueue4PoolWorker(int size) {
		this.queue = new ArrayBlockingQueue<T>(size);
	}

	public T poll() {
		try {
			return queue.poll(16, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			DebugUtil.debug(e);
		}
		return null;
	}
	
	public T poll2() {
		return queue.poll();
	}

	public void offer(T t) {
		queue.offer(t);
	}

	public boolean empty() {
		return queue.size() == 0;
	}

}

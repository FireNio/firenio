package com.gifisan.mtp.component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ArrayBlockingQueue4PoolWorker<T> implements Queue<T> {

	private java.util.concurrent.ArrayBlockingQueue<T> queue = null;
	
	public ArrayBlockingQueue4PoolWorker(int size) {
		this.queue = new ArrayBlockingQueue<T>(size);
	}

	public T poll() {
		try {
			return queue.poll(16, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
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

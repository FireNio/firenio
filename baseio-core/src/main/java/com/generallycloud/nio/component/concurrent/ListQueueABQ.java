package com.generallycloud.nio.component.concurrent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;

public class ListQueueABQ<T> implements ListQueue<T> {

	private ArrayBlockingQueue<T>	queue;
	private Logger				logger	= LoggerFactory.getLogger(ListQueueABQ.class);

	public ListQueueABQ(int capacity) {
		this.queue = new ArrayBlockingQueue<T>(capacity);
	}
	
	public boolean offer(T object) {
		return queue.offer(object);
	}

	public T poll() {
		return queue.poll();
	}

	public T poll(long timeout) {
		try {
			return queue.poll(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			logger.debug(e);
			return null;
		}
	}

	public int size() {
		return queue.size();
	}

}

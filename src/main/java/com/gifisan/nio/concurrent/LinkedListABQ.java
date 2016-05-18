package com.gifisan.nio.concurrent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;

public class LinkedListABQ<T> implements LinkedList<T> {

	private ArrayBlockingQueue<T>	queue	= null;
	private Logger				logger	= LoggerFactory.getLogger(LinkedListABQ.class);

	public LinkedListABQ(int capacity) {
		this.queue = new ArrayBlockingQueue<T>(capacity);
	}

	public boolean offer(T object) {
		return queue.offer(object);
	}

	public void forceOffer(T object) {
		for (; !queue.offer(object);) {

		}
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
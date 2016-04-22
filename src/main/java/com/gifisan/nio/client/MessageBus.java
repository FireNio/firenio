package com.gifisan.nio.client;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.gifisan.nio.component.ReadFuture;

public class MessageBus {

	private ReadFuture		response	= null;
	private ReentrantLock	lock		= new ReentrantLock();
	private Condition		notNull	= lock.newCondition();

	public ReadFuture poll(long timeout) {
		if (response == null) {
			ReentrantLock lock = this.lock;

			lock.lock();

			try {
				notNull.await(timeout, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
				notNull.signal();
			}

			lock.unlock();

			return response;
		}
		return response;
	}

	public void offer(ReadFuture response) {

		ReentrantLock lock = this.lock;

		lock.lock();

		this.response = response;

		notNull.signal();

		lock.unlock();

	}

}

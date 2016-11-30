package com.generallycloud.nio.component.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Waiter<T>{

	private ReentrantLock	lock		= new ReentrantLock();
	private Condition		callback	= lock.newCondition();
	private boolean		isDnoe;
	private boolean		timeouted;
	private T				t;
	
	/**
	 * @param timeout
	 * @return timeouted
	 */
	public boolean await() {

		ReentrantLock lock = this.lock;

		lock.lock();

		if (isDnoe) {

			lock.unlock();

			return false;
		}

		try {
			callback.await();
		} catch (InterruptedException e) {
			callback.signal();
		}

		timeouted = !isDnoe;

		lock.unlock();

		return timeouted;
	}
	

	/**
	 * @param timeout
	 * @return timeouted
	 */
	public boolean await(long timeout) {

		ReentrantLock lock = this.lock;

		lock.lock();

		if (isDnoe) {

			lock.unlock();

			return false;
		}

		try {
			callback.await(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			callback.signal();
		}

		timeouted = !isDnoe;

		lock.unlock();

		return timeouted;
	}

	public void setPayload(T t) {
		ReentrantLock lock = this.lock;

		lock.lock();

		this.isDnoe = true;

		this.t = t;

		callback.signal();

		lock.unlock();
	}
	
	public boolean isDnoe() {
		return isDnoe;
	}

	public T getPayload() {
		return t;
	}

	public boolean isTimeouted() {
		return timeouted;
	}
}

package com.generallycloud.nio.component.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Waiter<T> {

	private ReentrantLock	lock		= new ReentrantLock();
	private Condition		callback	= lock.newCondition();
	private Condition		lightWait	= lock.newCondition();
	private boolean		callbacked;
	private boolean		timeouted;
	private T				t;

	/**
	 * @param timeout
	 * @return timeouted
	 */
	public boolean await(long timeout) {

		ReentrantLock lock = this.lock;

		lock.lock();

		if (callbacked) {

			lock.unlock();

			return false;
		}

		try {
			callback.await(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			callback.signal();
		}

		timeouted = !callbacked;

		lock.unlock();

		return timeouted;
	}

	
	public boolean wait4Callback(long timeout) {

		ReentrantLock lock = this.lock;

		lock.lock();

		if (callbacked) {

			lock.unlock();

			return true;
		}

		try {
			lightWait.await(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			lightWait.signal();
		}

		lock.unlock();

		return callbacked;
	}
	
	public void setPayload(T t) {
		ReentrantLock lock = this.lock;

		lock.lock();

		this.callbacked = true;

		this.t = t;

		callback.signal();

		lightWait.signal();

		lock.unlock();
	}
	
	public T getPayload() {
		return t;
	}

	public boolean isTimeouted() {
		return timeouted;
	}
}

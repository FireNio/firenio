package com.gifisan.nio.component.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

//FIXME complete
public class Waiter<T> {

	private ReentrantLock	lock		= new ReentrantLock();
	private Condition		callback	= lock.newCondition();
	private Condition		lightWait	= lock.newCondition();
	private boolean		callbacked;
	private boolean		timeouted;
	private T				t;
	private boolean		success;

	/**
	 * 触发callback则返回true，否则返回false
	 * 
	 * @param timeout
	 * @return
	 */
	public boolean await(long timeout) {

		ReentrantLock lock = this.lock;

		lock.lock();

		if (callbacked) {

			lock.unlock();

			return true;
		}

		try {
			callback.await(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			callback.signal();
		}

		timeouted = !callbacked;

		lock.unlock();

		return success;
	}

	public boolean lightWait(long timeout) {

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
		setPayload(t, true);
	}

	public void setPayload(T t,boolean success) {
		ReentrantLock lock = this.lock;

		lock.lock();

		if (!timeouted) {
			
			this.success = success;
		}

		this.callbacked = true;

		this.t = t;

		callback.signal();

		lightWait.signal();

		lock.unlock();
	}
	
	public T getPayload() {
		return t;
	}

	public boolean isSuccess() {
		return success;
	}
}

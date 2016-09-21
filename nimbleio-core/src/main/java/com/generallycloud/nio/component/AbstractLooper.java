package com.generallycloud.nio.component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.Looper;

public abstract class AbstractLooper implements Looper {

	private ReentrantLock	lock		= new ReentrantLock();

	private Condition		wakeup	= lock.newCondition();

	public void stop() {

		ReentrantLock lock = this.lock;

		lock.lock();

		try {
			
			wakeup.signal();
			
		}finally {
			
			lock.unlock();
		}
	}

	protected void sleep(long time){
		
		ReentrantLock lock = this.lock;

		lock.lock();

		try {
			wakeup.await(time, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			throw new RuntimeException(e.getMessage(),e);
		}

		lock.unlock();
	}

}

package com.gifisan.nio.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.gifisan.nio.Looper;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;

//FIXME looper
public class TaskExecutor implements Looper {

	private long			interval	= 0;
	private Runnable		job		= null;
	private boolean		working	= true;
	private ReentrantLock	lock		= new ReentrantLock();
	private Condition		wait		= lock.newCondition();
	private Logger			logger	= LoggerFactory.getLogger(TaskExecutor.class);

	public TaskExecutor(Runnable job, long interval) {
		this.job = job;
		this.interval = interval;
	}

	public void loop() {

		ReentrantLock lock = this.lock;

		lock.lock();

		try {
			wait.await(interval, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			logger.debug(e);
		}

		lock.unlock();

		if (working) {
			job.run();
		}
	}

	public void stop() {
		this.working = false;

		ReentrantLock lock = this.lock;

		lock.lock();

		wait.signal();

		lock.unlock();
	}
}

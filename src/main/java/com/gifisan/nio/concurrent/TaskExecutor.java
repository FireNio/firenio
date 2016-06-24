package com.gifisan.nio.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.gifisan.nio.Stopable;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;

public class TaskExecutor implements Runnable ,Stopable {

	private long			interval	= 0;
	private Thread			thread	= null;
	private Runnable		job		= null;
	private boolean		running	= true;
	private ReentrantLock	lock		= new ReentrantLock();
	private Condition		wait		= lock.newCondition();
	private Logger			logger	= LoggerFactory.getLogger(TaskExecutor.class);

	public TaskExecutor(Runnable job, String name, long interval) {
		this.job = job;
		this.interval = interval;
		this.thread = new Thread(this, name);
	}

	public void run() {
		long interval = this.interval;
		Runnable job = this.job;
		for (;;) {

			ReentrantLock lock = this.lock;

			lock.lock();

			try {
				wait.await(interval, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				logger.debug(e);
			}

			lock.unlock();

			if (running) {
				job.run();
			} else {
				break;
			}
		}
	}

	public void start() {
		this.thread.start();

	}

	public void stop() {
		this.running = false;

		ReentrantLock lock = this.lock;

		lock.lock();

		wait.signal();

		lock.unlock();
	}
}

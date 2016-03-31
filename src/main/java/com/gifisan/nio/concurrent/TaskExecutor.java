package com.gifisan.nio.concurrent;

import com.gifisan.nio.LifeCycle;
import com.gifisan.nio.common.DebugUtil;

public class TaskExecutor implements Runnable ,LifeCycle {

	private long		interval	= 0;
	private Runnable	job		= null;
	private byte[]	lock		= { 0 };
	private boolean	running	= true;
	private Thread		thread	= null;

	public TaskExecutor(Runnable job, String name, long interval) {
		this.job = job;
		this.interval = interval;
		this.thread = new Thread(this, name);
	}

	public void run() {
		long interval = this.interval;
		byte[] lock = this.lock;
		Runnable job = this.job;
		for (;;) {
			try {
				synchronized (lock) {
					lock.wait(interval);
				}
			} catch (InterruptedException e) {
				DebugUtil.debug(e);
			}
			if (running) {
				job.run();
			} else {
				break;
			}
		}
	}

	public void start() throws Exception {
		this.thread.start();

	}

	public void stop() throws Exception {
		this.running = false;
		synchronized (lock) {
			lock.notify();
		}
	}
}

package com.gifisan.nio.concurrent;

import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.schedule.Job;

public class TaskExecutor extends AbstractLifeCycle implements Runnable {

	private long		interval	= 0;
	private Job		job		= null;
	private byte[]	lock		= { 0 };
	private boolean	running	= true;
	private Thread		thread	= null;

	public TaskExecutor(Job job, String name, long interval) {
		this.job = job;
		this.interval = interval;
		this.thread = new Thread(this, name);
	}

	public void run() {
		long interval = this.interval;
		byte [] lock = this.lock;
		Job job = this.job;
		for (;;) {
			try {
				synchronized (lock) {
					lock.wait(interval);
				}
			} catch (InterruptedException e) {
				DebugUtil.debug(e);
			}
			if (running) {
				job.schedule();
			}else{
				break;
			}
		}
	}

	protected void doStart() throws Exception {
		this.thread.start();
		
	}

	protected void doStop() throws Exception {
		this.running = false;
		synchronized (lock) {
			lock.notify();
		}
	}

}

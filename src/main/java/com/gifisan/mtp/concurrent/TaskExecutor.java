package com.gifisan.mtp.concurrent;

import com.gifisan.mtp.schedule.Job;

public class TaskExecutor implements Runnable {

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
				e.printStackTrace();
			}
			if (running) {
				job.schedule();
			}else{
				break;
			}
		}
	}

	public void start() {
		this.thread.start();
	}

	public void stop() {
		this.running = false;
		synchronized (lock) {
			lock.notify();
		}
	}

}

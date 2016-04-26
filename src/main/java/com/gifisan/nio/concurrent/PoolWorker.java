package com.gifisan.nio.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.gifisan.nio.AbstractLifeCycle;

public class PoolWorker extends AbstractLifeCycle implements Runnable {

	private boolean					working	= false;
	private BlockingQueue<Runnable>	jobs		= null;

	public PoolWorker(BlockingQueue<Runnable> jobs) {
		this.jobs = jobs;
	}

	public void run() {
		for (; isRunning();) {
			working = true;
			Runnable job = null;
			try {
				job = jobs.poll(16,TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (job != null) {
				job.run();
			}
			working = false;
		}
	}

	protected void doStart() throws Exception {

	}

	protected void doStop() throws Exception {
		for (; working;) {
			Thread.sleep(8);
		}
		// new Exception("test").printStackTrace();
	}

}
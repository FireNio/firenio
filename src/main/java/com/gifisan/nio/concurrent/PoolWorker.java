package com.gifisan.nio.concurrent;

import com.gifisan.nio.AbstractLifeCycle;

public class PoolWorker extends AbstractLifeCycle implements Runnable {

	private boolean			working	= false;
	private LinkedList<Runnable>	jobs		= null;

	public PoolWorker(LinkedList<Runnable> jobs) {
		this.jobs = jobs;
	}

	public void run() {
		for (; isRunning();) {
			
			working = true;
			
			Runnable job = null;
			
			job = jobs.poll(16);

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
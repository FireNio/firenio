package com.gifisan.nio.component.concurrent;

import com.gifisan.nio.Looper;

public class PoolWorker implements Looper {

	private LinkedList<Runnable>	jobs		;

	public PoolWorker(LinkedList<Runnable>	jobs) {
		this.jobs = jobs;
	}

	//FIXME left jobs
	public void stop() {
		
	}

	public void loop() {
		
		Runnable job = null;
		
		job = jobs.poll(16);

		if (job != null) {
			job.run();
		}
	}
	
	public void dispatch(Runnable job){
		this.jobs.offer(job);
	}
}
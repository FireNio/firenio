package com.generallycloud.nio.component.concurrent;

import com.generallycloud.nio.Looper;

public class PoolWorker implements Looper {

	private ListQueue<Runnable>	jobs		;

	public PoolWorker(ListQueue<Runnable>	jobs) {
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
package com.yoocent.mtp.component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.yoocent.mtp.AbstractLifeCycle;
import com.yoocent.mtp.schedule.Job;
import com.yoocent.mtp.schedule.ScheduleAble;
import com.yoocent.mtp.schedule.ScheduleJob;

public class PoolWorker extends AbstractLifeCycle implements Runnable , ScheduleJob{
	
	private boolean working = false;
	
	private ArrayBlockingQueue<Job> jobs = null;
	
	public PoolWorker(ArrayBlockingQueue<Job> jobs) {
		this.jobs = jobs;
	}
	
	private Job job = null;

	public void run() {
		//OUTER:
		while (isRunning()) {
			try {
				working = true;
				Job job = jobs.poll(16,TimeUnit.MILLISECONDS);
				if (job != null) {
					job.run();
					/*
					
					ScheduleAble schedule = job.getScheduleAble();
					if(schedule == null){
						continue;
					}
					
					synchronized (schedule) {
						job = schedule.schedule();
						if (job == null) {
							schedule.pollSchedule();
							working = false;
							continue;
						}
					}
					while(job != null){
						job.run();
						synchronized (schedule) {
							job = schedule.schedule();
							if (job == null) {
								schedule.pollSchedule();
								working = false;
								continue OUTER;
							}
						}
					}
					*/
				}
				
				working = false;
			} catch (InterruptedException e1) {
				e1.printStackTrace();
				working = false;
			}
		}
	}

	protected void doStart() throws Exception {
		
	}

	protected void doStop() throws Exception {
		while(working){
			Thread.sleep(8);
		}
	}
	
	public void schedule(Job job) {
		this.job = job;
	}

	public Job schedule() {
		return this.job;
	}

	
}
package com.gifisan.nio;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class UniqueThread implements Runnable {

	private BlockingQueue<Runnable>	jobs		= new ArrayBlockingQueue<Runnable>(999);
	private boolean				running	= false;
	private Thread					thread	= null;

	public void execute(Runnable job) {
		jobs.offer(job);
	}

	public void start() {
		this.running = true;
		this.thread = new Thread(this);
		this.thread.start();
	}

	public void run() {
		for (; running;) {
			try {
				Runnable job = jobs.poll(1000, TimeUnit.MILLISECONDS);
				if (job == null) {
					continue;
				}
				try {
					job.run();
				} catch (Throwable e) {
					e.printStackTrace();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void stop(){
		this.running = false;
	}
}

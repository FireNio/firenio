package com.gifisan.nio;

import com.gifisan.nio.concurrent.LinkedList;
import com.gifisan.nio.concurrent.LinkedListM2O;

public class UniqueThread implements Runnable {

	private LinkedList<Runnable>	jobs		= new LinkedListM2O<Runnable>(999);
	private boolean				running	= false;
	private Thread					thread	= null;

	public void execute(Runnable runnable) {
		jobs.forceOffer(runnable);
	}

	public void start() {
		this.running = true;
		this.thread = new Thread(this);
		this.thread.start();
	}

	public void run() {
		for (; running;) {
			Runnable job = jobs.poll(1000);
			if (job == null) {
				continue;
			}
			try {
				job.run();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	public void stop() {
		this.running = false;
	}
}

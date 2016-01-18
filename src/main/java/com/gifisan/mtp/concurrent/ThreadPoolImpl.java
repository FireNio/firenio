package com.gifisan.mtp.concurrent;

import java.util.ArrayList;
import java.util.List;

import com.gifisan.mtp.AbstractLifeCycle;
import com.gifisan.mtp.common.LifeCycleUtil;
import com.gifisan.mtp.schedule.Job;

public class ThreadPoolImpl extends AbstractLifeCycle implements ThreadPool {

	private class LifedPoolWorker extends Thread {

		private PoolWorker	worker	= null;

		public LifedPoolWorker(PoolWorker worker, String name) {
			super(ThreadPoolImpl.this.threadPrefix + "@PoolWorker-" + name);
			this.worker = worker;
		}

		public void run() {
			this.worker.run();
		}

		public void startWork() throws Exception {
			this.worker.start();
			this.start();

		}

		public void stopWork() throws Exception {
			LifeCycleUtil.stop(worker);
		}

	}

	private Queue<Job>			jobs			= null;
	private int				size			= 4;
	private String				threadPrefix	= null;
	private List<LifedPoolWorker>	workers		= new ArrayList<ThreadPoolImpl.LifedPoolWorker>(size);

	/**
	 * default size 4
	 * 
	 * @param threadPrefix
	 */
	public ThreadPoolImpl(Queue<Job> jobs, String threadPrefix) {
		this.jobs = jobs;
		this.size = 4;
		this.threadPrefix = threadPrefix;
	}

	public ThreadPoolImpl(Queue<Job> jobs, String threadPrefix, int size) {
		this.jobs = jobs;
		this.size = size;
		this.threadPrefix = threadPrefix;

	}

	public void dispatch(Job job) {
		if (!isStarted()) {
			// free time, ignore job
			return;
		}

		jobs.offer(job);
	}

	protected void doStart() throws Exception {
		synchronized (workers) {
			for (int i = 0; i < size; i++) {
				LifedPoolWorker lifedPoolWorker = produceWorker(i);
				workers.add(lifedPoolWorker);
			}
			for (LifedPoolWorker worker : workers) {
				try {
					worker.startWork();
				} catch (Exception e) {
					e.printStackTrace();
					workers.remove(worker);
				}
			}
		}
	}

	protected void doStop() throws Exception {
		while (!jobs.empty()) {
			Thread.sleep(64);
		}
		synchronized (workers) {
			for (LifedPoolWorker worker : workers) {
				try {
					worker.stopWork();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private LifedPoolWorker produceWorker(int index) {
		PoolWorker worker = new PoolWorker(this.jobs);
		LifedPoolWorker lifedPoolWorker = new LifedPoolWorker(worker, String.valueOf(index));
		return lifedPoolWorker;
	}
}
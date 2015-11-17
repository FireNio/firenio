package com.yoocent.mtp.component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import com.yoocent.mtp.AbstractLifeCycle;
import com.yoocent.mtp.common.LifeCycleUtil;
import com.yoocent.mtp.schedule.Job;

public final class BlockingQueueThreadPool extends AbstractLifeCycle implements ThreadPool{
	
	private int size = 5;

	private ArrayBlockingQueue<Job> jobs = new ArrayBlockingQueue<Job>(999999);

	private List<LifedPoolWorker> workers = new ArrayList<BlockingQueueThreadPool.LifedPoolWorker>(size);
	
	protected void doStart() throws Exception {
		synchronized (workers) {
			for (int i = 0; i < size; i++) {
				LifedPoolWorker lifedPoolWorker = produceWorker();
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
	
	private LifedPoolWorker produceWorker(){
		PoolWorker worker = new PoolWorker(this.jobs);
		LifedPoolWorker lifedPoolWorker = new LifedPoolWorker(worker);
		return lifedPoolWorker;
	}

	protected void doStop() throws Exception {
		while (jobs.size() > 0) {
			Thread.sleep(3000);
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

	public void dispatch(Job job) {
		if (!isStarted()) {
			//free time, ignore job
			return;
		}
		jobs.add(job);
	}

	private class LifedPoolWorker extends Thread{
		
		public LifedPoolWorker(PoolWorker worker) {
			super("accept-job@PoolWorker@"+Integer.toHexString(worker.hashCode()));
			this.worker = worker;
		}

		private PoolWorker worker = null;
		
		public void run() {
			this.worker.run();
		}
		
		public void startWork() throws Exception{
			this.worker.start();
			this.start();
			
		}
		
		public void stopWork() throws Exception{
			LifeCycleUtil.stop(worker);
		}
		
	}
}
package com.gifisan.mtp.component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import com.gifisan.mtp.AbstractLifeCycle;
import com.gifisan.mtp.common.LifeCycleUtil;
import com.gifisan.mtp.schedule.Job;

public final class BlockingQueueThreadPool extends AbstractLifeCycle implements ThreadPool{
	
	private class LifedPoolWorker extends Thread{
		
		private PoolWorker worker = null;

		public LifedPoolWorker(PoolWorker worker,String name) {
//			super("accept-job@PoolWorker@"+Integer.toHexString(worker.hashCode()));
			super(BlockingQueueThreadPool.this.threadPrefix+"@PoolWorker-"+name);
			this.worker = worker;
		}
		
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
	
	private ArrayBlockingQueue<Job> jobs	= new ArrayBlockingQueue<Job>(999999);
	private int size                     	= 0;
	private String threadPrefix 			= null;
	private List<LifedPoolWorker> workers  = new ArrayList<BlockingQueueThreadPool.LifedPoolWorker>(size);

	/**
	 * default size 4
	 * 
	 * @param threadPrefix
	 */
	public BlockingQueueThreadPool(String threadPrefix) {
		this.size 			= 4;
		this.threadPrefix	= threadPrefix;
	}
	
	public BlockingQueueThreadPool(String threadPrefix,int size) {
		this.size 			= size;
		this.threadPrefix	= threadPrefix;
		
	}
	
	public void dispatch(Job job) {
		if (!isStarted()) {
			//free time, ignore job
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
		while (jobs.size() > 0) {
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

	private LifedPoolWorker produceWorker(int index){
		PoolWorker worker = new PoolWorker(this.jobs);
		LifedPoolWorker lifedPoolWorker = new LifedPoolWorker(worker,String.valueOf(index));
		return lifedPoolWorker;
	}
}
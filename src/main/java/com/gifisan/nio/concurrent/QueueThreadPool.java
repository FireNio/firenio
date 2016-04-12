package com.gifisan.nio.concurrent;

import java.util.ArrayList;
import java.util.List;

import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.common.LifeCycleUtil;

public class QueueThreadPool extends AbstractLifeCycle implements ThreadPool {

	private class LifedPoolWorker extends Thread {

		private PoolWorker	worker	= null;

		public LifedPoolWorker(PoolWorker worker, String name) {
			super(QueueThreadPool.this.threadPrefix + "@PoolWorker-" + name);
			this.worker = worker;
		}

		public void run() {
			this.worker.run();
		}

		public void startWork() throws Exception {
			this.worker.start();
			this.start();

		}

		public void stopWork() {
			LifeCycleUtil.stop(worker);
		}

	}

	private LinkedList<Runnable>		jobs		= null;
	private int				size			= 4;
	private String				threadPrefix	= null;
	private List<LifedPoolWorker>	workers		= new ArrayList<QueueThreadPool.LifedPoolWorker>(size);

	/**
	 * default size 4
	 * 
	 * @param threadPrefix
	 */
	public QueueThreadPool(String threadPrefix) {
		this(threadPrefix,4);
	}

	public QueueThreadPool(String threadPrefix, int size) {
		this.size = size;
		this.threadPrefix = threadPrefix;
		this.jobs = new LinkedListO2M<Runnable>(1024*1000);
	}

	public void dispatch(Runnable job) {
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
					DebugUtil.debug(e);
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
					DebugUtil.debug(e);
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
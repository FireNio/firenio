package com.gifisan.nio.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;

public class QueueThreadPool extends AbstractLifeCycle implements ThreadPool {
	
	private Logger	logger		= LoggerFactory.getLogger(QueueThreadPool.class);

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

	private LinkedList<Runnable>			jobs			= null;
	private int						size			= 4;
	private String						threadPrefix	= null;
	private ReentrantLock				lock			= new ReentrantLock();
	private List<LifedPoolWorker>			workers		= new ArrayList<QueueThreadPool.LifedPoolWorker>(size);

	/**
	 * default size 4
	 * 
	 * @param threadPrefix
	 */
	public QueueThreadPool(String threadPrefix) {
		this(threadPrefix, 4);
	}

	public QueueThreadPool(String threadPrefix, int size) {
		this.size = size;
		this.threadPrefix = threadPrefix;
		this.jobs = new LinkedListABQ<Runnable>(1024 * 8);
	}

	public void dispatch(Runnable runnable) {
		if (!isStarted()) {
			// free time, ignore job
			return;
		}

		for (; !jobs.offer(runnable);) {

		}

	}

	protected void doStart() throws Exception {
		ReentrantLock lock = this.lock;

		lock.lock();

		try {

			for (int i = 0; i < size; i++) {
				LifedPoolWorker lifedPoolWorker = produceWorker(i);
				workers.add(lifedPoolWorker);
			}
			for (LifedPoolWorker worker : workers) {
				try {
					worker.startWork();
				} catch (Exception e) {
					logger.debug(e);
					workers.remove(worker);
				}
			}
		} finally {

			lock.unlock();
		}

	}

	protected void doStop() throws Exception {
		while (jobs.size() > 0) {
			Thread.sleep(64);
		}
		ReentrantLock lock = this.lock;

		lock.lock();

		for (LifedPoolWorker worker : workers) {
			try {
				worker.stopWork();
			} catch (Exception e) {
				logger.debug(e);
			}
		}
		lock.unlock();
	}

	private LifedPoolWorker produceWorker(int index) {
		PoolWorker worker = new PoolWorker(this.jobs);
		LifedPoolWorker lifedPoolWorker = new LifedPoolWorker(worker, String.valueOf(index));
		return lifedPoolWorker;
	}
}
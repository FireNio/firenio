package com.generallycloud.nio.component.concurrent;

import com.generallycloud.nio.AbstractLifeCycle;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;

public class QueueThreadPool extends AbstractLifeCycle implements ThreadPool {

	private Logger				logger	= LoggerFactory.getLogger(QueueThreadPool.class);
	private int				core_size;
	private String				threadPrefix;
	private UniqueThread[]		poolWorkerThreads;
	private PoolWorker[]		poolWorks;
	private FixedAtomicInteger	indexer;
	private LinkedList<Runnable> jobs;

	public QueueThreadPool(String threadPrefix, int core_size) {
		this.core_size = core_size;
		this.threadPrefix = threadPrefix;
		this.poolWorks = new PoolWorker[core_size];
		this.poolWorkerThreads = new UniqueThread[core_size];
		this.indexer = new FixedAtomicInteger(core_size - 1);
		this.jobs = new LinkedListABQ<Runnable>(1024 * 10240);
	}

	public void dispatch(Runnable runnable) {
		if (!isStarted()) {
			throw new IllegalStateException("stopped");
		}

		poolWorks[indexer.getAndIncrement()].dispatch(runnable);
	}

	protected void doStart() throws Exception {

		for (int i = 0; i < core_size; i++) {
			
			PoolWorker poolWorker = new PoolWorker(jobs);

			String threadName = threadPrefix + "-" + i;
			
			UniqueThread poolWorkerThrad = new UniqueThread(poolWorker, threadName);

			poolWorkerThreads[i] = poolWorkerThrad;
			poolWorks[i] = poolWorker;
			
			poolWorkerThrad.start();
		}
	}

	protected void doStop() throws Exception {

		for (UniqueThread worker : poolWorkerThreads) {
			try {
				worker.stop();
			} catch (Exception e) {
				logger.debug(e);
			}
		}
	}
}
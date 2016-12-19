package com.generallycloud.nio.component.concurrent;

import java.util.concurrent.RejectedExecutionException;

import com.generallycloud.nio.AbstractLifeCycle;
import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.AbstractEventLoopThread;

public class ThreadEventLoop extends AbstractLifeCycle implements EventLoop {

	private static Logger		logger				= LoggerFactory.getLogger(ThreadEventLoop.class);

	private String				threadName			= null;

	private SingleEventLoopWorker	singleEventLoopWorker	= null;

	public ThreadEventLoop(String threadName, int queueSize) {
		
		this.threadName = threadName;

		this.singleEventLoopWorker = new SingleEventLoopWorker(queueSize);
	}

	@Override
	public void dispatch(Runnable job) {
		singleEventLoopWorker.dispatch(job);
	}

	@Override
	protected void doStart() throws Exception {
		singleEventLoopWorker.startup(threadName);
	}

	@Override
	protected void doStop() throws Exception {
		LifeCycleUtil.stop(singleEventLoopWorker);
	}

	@Override
	public String toString() {
		return singleEventLoopWorker.toString();
	}

	// AtomicInteger integer = new AtomicInteger();

	class SingleEventLoopWorker extends AbstractEventLoopThread {

		private boolean stoped = false;

		protected SingleEventLoopWorker(int queueSize) {
			this.jobs = new ListQueueABQ<Runnable>(queueSize);
		}

		private ListQueue<Runnable> jobs;

		public void dispatch(Runnable job) {

			// logger.debug("dispatch {}",integer.incrementAndGet());

			if (stoped || !jobs.offer(job)) {
				throw new RejectedExecutionException();
			}
		}

		@Override
		public void doLoop() {

			try {

				Runnable runnable = jobs.poll(32);

				if (runnable == null) {
					return;
				}

				runnable.run();
			} catch (Throwable e) {
				logger.error(e.getMessage(), e);
			}
		}
		
		@Override
		protected void beforeStop() {
			for (; jobs.size() > 0;) {
				ThreadUtil.sleep(8);
			}
			super.beforeStop();
		}
	}

	@Override
	public boolean inEventLoop() {
		return Thread.currentThread() == singleEventLoopWorker.getMonitor();
	}

}

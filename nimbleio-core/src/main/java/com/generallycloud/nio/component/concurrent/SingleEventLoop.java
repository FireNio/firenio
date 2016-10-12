package com.generallycloud.nio.component.concurrent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import com.generallycloud.nio.AbstractLifeCycle;
import com.generallycloud.nio.Looper;
import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.ThreadUtil;

public class SingleEventLoop extends AbstractLifeCycle implements EventLoop {

	private static Logger		logger	= LoggerFactory.getLogger(SingleEventLoop.class);

	private EventLoopThread			thread;

	private SingleEventLoopWorker	singleEventLoopWorker;

	public SingleEventLoop(String threadName, int queueSize) {

		this.singleEventLoopWorker = new SingleEventLoopWorker(queueSize);

		this.thread = new EventLoopThread(singleEventLoopWorker, threadName);
	}

	public void dispatch(Runnable job) {
		singleEventLoopWorker.dispatch(job);
	}

	protected void doStart() throws Exception {
		thread.start();
	}

	protected void doStop() throws Exception {
		LifeCycleUtil.stop(thread);
	}

	public String toString() {
		return thread.toString();
	}

	class SingleEventLoopWorker implements Looper {

		private boolean	stoped	= false;

		protected SingleEventLoopWorker(int queueSize) {
			this.jobs = new ArrayBlockingQueue<Runnable>(queueSize);
		}

		private ArrayBlockingQueue<Runnable>	jobs;

		public void dispatch(Runnable job) {

			if (stoped || !jobs.offer(job)) {
				throw new RejectedExecutionException();
			}
		}

		public void loop() {

			try {

				Runnable runnable = jobs.poll(32, TimeUnit.MILLISECONDS);

				if (runnable == null) {
					return;
				}

				runnable.run();
			} catch (Throwable e) {
				logger.error(e.getMessage(), e);
			}
		}

		public void stop() {

			stoped = true;

			for (; jobs.size() > 0;) {
				ThreadUtil.sleep(8);
			}
		}
	}

	public boolean inEventLoop(Thread thread) {
		return thread == this.thread.getMonitor();
	}
	
}

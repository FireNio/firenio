package com.generallycloud.nio.component.concurrent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import com.generallycloud.nio.AbstractLifeCycle;
import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.AbstractLooper;

public class SingleEventLoop extends AbstractLifeCycle implements EventLoop {

	private static Logger		logger	= LoggerFactory.getLogger(SingleEventLoop.class);

	private UniqueThread		thread;

	private RealSingleEventLoop	realSingleEventLoop;

	public SingleEventLoop(String threadName, int queueSize) {
		
		this.realSingleEventLoop = new RealSingleEventLoop(queueSize);
		
		this.thread = new UniqueThread(realSingleEventLoop, threadName);
	}

	public void dispatch(Runnable job) {
		realSingleEventLoop.dispatch(job);
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

	class RealSingleEventLoop extends AbstractLooper {

		protected RealSingleEventLoop(int queueSize) {
			this.jobs = new ArrayBlockingQueue<Runnable>(queueSize);
		}

		private ArrayBlockingQueue<Runnable>	jobs;

		public void dispatch(Runnable job) {

			if (!jobs.offer(job)) {
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
	}
}

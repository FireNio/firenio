package com.generallycloud.nio.component;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.concurrent.EventLoopThread;

public abstract class AbstractEventLoopThread implements EventLoopThread {

	private static final Logger		logger		= LoggerFactory.getLogger(AbstractEventLoopThread.class);

	private volatile boolean		running		= false;

	private boolean				working		= false;

	private boolean				stoping		= false;

	private byte[]				workingLock	= new byte[] {};

	private Thread					monitor 		= null;

	@Override
	public void loop() {

		for (;;) {

			if (!running) {
				notify4Free();
				return;
			}

			working = true;

			if (stoping) {
				working = false;
				notify4Free();
				return;
			}

			try {

				doLoop();

			} catch (Throwable e) {
				logger.error(e.getMessage(), e);
			}

			working = false;
		}
	}

	protected abstract void doLoop();

	protected void beforeStop() {
	}

	@Override
	public void stop() {

		synchronized (this) {

			if (!running) {
				return;
			}

			try {
				beforeStop();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}

			running = false;

			stoping = true;

			if (working) {

				notify4Free();

				wakeupThread();

				wait4Free();
			}

			stoping = false;

			doStop();
		}
	}

	protected void doStop() {
	}

	private void wait4Free() {
		synchronized (workingLock) {
			if (working) {
				try {
					workingLock.wait();
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
	}

	private void notify4Free() {
		synchronized (workingLock) {
			workingLock.notify();
		}
	}

	protected void sleep(long time) {
		synchronized (workingLock) {
			try {
				workingLock.wait(time);
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	protected void wakeupThread() {
	}

	@Override
	public void startup(String threadName) throws Exception {

		synchronized (this) {

			if (running) {
				return;
			}

			running = true;

			this.monitor = new Thread(new Runnable() {

				@Override
				public void run() {
					loop();
				}
			}, threadName);

			this.doStartup();

			this.monitor.start();
		}
	}

	@Override
	public boolean isMonitor(Thread thread) {
		return monitor == thread;
	}

	@Override
	public Thread getMonitor() {
		return monitor;
	}

	protected void doStartup() throws Exception {

	}

	@Override
	public boolean isRunning() {
		return running;
	}

	@Override
	public boolean isStopping() {
		return stoping;
	}

}

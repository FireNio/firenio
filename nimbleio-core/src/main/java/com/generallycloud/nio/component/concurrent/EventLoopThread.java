package com.generallycloud.nio.component.concurrent;

import java.util.concurrent.atomic.AtomicBoolean;

import com.generallycloud.nio.Looper;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;

public class EventLoopThread implements Looper {

	private boolean		running		= false;
	private AtomicBoolean	initialized	= new AtomicBoolean();
	private Logger			logger		= LoggerFactory.getLogger(EventLoopThread.class);
	private Looper			looper;
	private Thread			monitor;
	private String			threadName;
	
	public EventLoopThread(Looper looper, String threadName) {
		this.looper = looper;
		this.threadName = threadName;
	}

	public void start() {

		if (!initialized.compareAndSet(false, true)) {
			return;
		}

		this.running = true;

		monitor = new Thread(new Runnable() {

			public void run() {
				loop();
			}
		}, threadName);

		monitor.start();
	}

	public void loop() {

		Looper _looper = looper;

		for (; running;) {

			try {
				_looper.loop();
			} catch (Throwable e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	public void stop() {

		this.looper.stop();
		
		this.running = false;
	}

	public boolean isMonitor(Thread thread) {
		return monitor == thread;
	}
	
	public String toString() {
		return threadName;
	}
}

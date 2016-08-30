package com.gifisan.nio.component.concurrent;

import java.util.concurrent.atomic.AtomicBoolean;

import com.gifisan.nio.Looper;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;

public class UniqueThread implements Looper {

	private boolean		running		= false;
	private AtomicBoolean	initialized	= new AtomicBoolean();
	private Logger			logger		= LoggerFactory.getLogger(UniqueThread.class);
	private Looper			looper;
	private Thread			monitor;
	private String			threadName;
	
	public UniqueThread(Looper looper, String threadName) {
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

		this.running = false;

		Looper looper = this.looper;

		if (looper == null) {
			return;
		}

		looper.stop();
	}

	public boolean isMonitor(Thread thread) {
		return monitor == thread;
	}
}

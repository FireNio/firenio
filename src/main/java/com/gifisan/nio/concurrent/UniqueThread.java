package com.gifisan.nio.concurrent;

import java.util.concurrent.atomic.AtomicBoolean;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;

public class UniqueThread {

	private boolean		running		= false;
	private Runnable		runnable		= null;
	private AtomicBoolean	initialized	= new AtomicBoolean();
	private Logger			logger		= LoggerFactory.getLogger(UniqueThread.class);

	public void start(Runnable runnable, String name) {

		if (!initialized.compareAndSet(false, true)) {
			return;
		}

		this.running = true;

		new Thread(new Runnable() {

			public void run() {

				Runnable runnable = UniqueThread.this.runnable;

				for (; running;) {

					try {
						runnable.run();
					} catch (Throwable e) {
						logger.error(e.getMessage(), e);
					}
				}

			}
		}, name).start();
	}

	public void stop() {
		this.running = false;
	}

}

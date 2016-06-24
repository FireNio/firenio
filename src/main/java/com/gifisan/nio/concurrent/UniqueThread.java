package com.gifisan.nio.concurrent;

import java.util.concurrent.atomic.AtomicBoolean;

import com.gifisan.nio.Stopable;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;

public class UniqueThread implements Stopable{

	private boolean		running		= false;
	private AtomicBoolean	initialized	= new AtomicBoolean();
	private Logger			logger		= LoggerFactory.getLogger(UniqueThread.class);

	public void start(final Runnable runnable, String name) {

		if (!initialized.compareAndSet(false, true)) {
			return;
		}

		this.running = true;

		new Thread(new Runnable() {

			public void run() {

				Runnable _runnable = runnable;

				for (; running;) {

					try {
						_runnable.run();
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

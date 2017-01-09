/*
 * Copyright 2015 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.generallycloud.nio.component.concurrent;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;

public abstract class AbstractEventLoop implements EventLoop {

	private static final Logger		logger	= LoggerFactory.getLogger(AbstractEventLoop.class);

	private volatile boolean		running	= false;

	private boolean				working	= false;

	private boolean				stoping	= false;

	private Thread					monitor	= null;

	@Override
	public void loop() {

		for (;;) {

			if (!running) {
				return;
			}

			working = true;

			if (stoping) {
				working = false;
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

			running = false;

			stoping = true;

			try {
				beforeStop();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}

			if (working) {
				wakeup();
			}

			doStop();
		}
	}

	protected void doStop() {
	}

	public void wakeup() {
	}

	@Override
	public void startup(String threadName) throws Exception {

		synchronized (this) {

			if (running) {
				return;
			}

			running = true;

			working = true;

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
	public boolean inEventLoop() {
		return inEventLoop(Thread.currentThread());
	}

	@Override
	public boolean inEventLoop(Thread thread) {
		return getMonitor() == thread;
	}
}

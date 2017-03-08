/*
 * Copyright 2015-2017 GenerallyCloud.com
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
package com.generallycloud.baseio.component.concurrent;

import com.generallycloud.baseio.common.Logger;
import com.generallycloud.baseio.common.LoggerFactory;
import com.generallycloud.baseio.live.AbstractLifeCycle;

public abstract class AbstractEventLoop implements EventLoop {

	private class SingleEventLoopGroup extends AbstractLifeCycle implements EventLoopGroup {

		private EventLoop eventLoop;

		public SingleEventLoopGroup(EventLoop eventLoop) {
			this.eventLoop = eventLoop;
		}

		@Override
		protected void doStart() throws Exception {

		}

		@Override
		protected void doStop() throws Exception {

		}

		@Override
		public EventLoop getNext() {
			return eventLoop;
		}
	}

	private static final Logger	logger				= LoggerFactory.getLogger(AbstractEventLoop.class);

	private Thread				monitor				= null;

	private volatile boolean	running				= false;

	private EventLoopGroup		singleEventLoopGroup	= new SingleEventLoopGroup(this);

	private boolean			stoping				= false;

	private boolean			working				= false;

	protected void beforeStop() {
	}

	protected abstract void doLoop();

	protected void doStartup() throws Exception {

	}

	protected void doStop() {
	}

	@Override
	public EventLoopGroup getEventLoopGroup() {
		return singleEventLoopGroup;
	}

	@Override
	public Thread getMonitor() {
		return monitor;
	}

	@Override
	public boolean inEventLoop() {
		return inEventLoop(Thread.currentThread());
	}

	@Override
	public boolean inEventLoop(Thread thread) {
		return getMonitor() == thread;
	}

	@Override
	public boolean isRunning() {
		return running;
	}

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

	public void wakeup() {
	}

}

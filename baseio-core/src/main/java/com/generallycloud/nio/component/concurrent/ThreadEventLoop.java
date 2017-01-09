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

import java.util.concurrent.RejectedExecutionException;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;

public class ThreadEventLoop extends AbstractEventLoop implements ExecutorEventLoop {

	private static Logger logger = LoggerFactory.getLogger(ThreadEventLoop.class);

	public ThreadEventLoop(int queueSize) {
		this.jobs = new ListQueueABQ<Runnable>(queueSize);
	}

	private boolean			stoped	= false;

	private ListQueue<Runnable>	jobs;

	public void dispatch(Runnable job) {

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
		//FIXME __clean jobs
		
		
		
		
		super.beforeStop();
	}

}

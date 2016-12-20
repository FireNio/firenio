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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.generallycloud.nio.AbstractLifeCycle;

public class ExecutorEventLoop extends AbstractLifeCycle implements EventLoop{

	private int		eventLoopSize		;
	private int		maxEventLoopSize	;
	private long		keepAliveTime		;
	private String		eventLoopName		;
	private int		maxEventQueueSize	;
	private NamedThreadFactory threadFactory;

	public ExecutorEventLoop(String eventLoopName,
			int eventLoopSize, 
			int maxEventLoopSize, 
			int maxEventQueueSize,
			long keepAliveTime) {
		this.eventLoopSize = eventLoopSize;
		this.maxEventLoopSize = maxEventLoopSize;
		this.maxEventQueueSize = maxEventQueueSize;
		this.keepAliveTime = keepAliveTime;
		this.eventLoopName = eventLoopName;
	}

	private ThreadPoolExecutor	poolExecutor	;

	@Override
	public void dispatch(Runnable job) {
		this.poolExecutor.execute(job);

	}

	@Override
	protected void doStart() throws Exception {

		threadFactory = new NamedThreadFactory(eventLoopName);

		poolExecutor = new ThreadPoolExecutor(eventLoopSize, maxEventLoopSize, keepAliveTime, TimeUnit.MILLISECONDS,
				new ArrayBlockingQueue<Runnable>(maxEventQueueSize), threadFactory);
	}

	@Override
	protected void doStop() throws Exception {
		
		if (poolExecutor != null) {
			poolExecutor.shutdown();
		}
	}

	@Override
	public boolean inEventLoop() {
		return threadFactory.inFactory(Thread.currentThread());
	}

}

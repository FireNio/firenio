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

import com.generallycloud.nio.AbstractLifeCycle;
import com.generallycloud.nio.common.LifeCycleUtil;

public abstract class AbstractEventLoopGroup extends AbstractLifeCycle implements EventLoopGroup{
	
	private String eventLoopName;
	
	private int eventQueueSize;
	
	private int eventLoopSize;
	
	private EventLoop []eventLoopArray;
	
	private FixedAtomicInteger eventLoopIndex;

	public AbstractEventLoopGroup(String eventLoopName, int eventQueueSize, int eventLoopSize) {
		this.eventLoopName = eventLoopName;
		this.eventQueueSize = eventQueueSize;
		this.eventLoopSize = eventLoopSize;
		this.eventLoopIndex = new FixedAtomicInteger(0, eventLoopSize - 1);
	}

	@Override
	public EventLoop getNext() {
		return eventLoopArray[eventLoopIndex.getAndIncrement()];
	}

	@Override
	protected void doStart() throws Exception {

		eventLoopArray = new EventLoop[eventLoopSize]; 
		
		for (int i = 0; i < eventLoopArray.length; i++) {
			
			String threadName = eventLoopName + "-" + i+"(max:"+eventQueueSize+")";
			
			eventLoopArray[i] = newEventLoop(threadName, eventQueueSize);
		}
		
		for(EventLoop el : eventLoopArray){
			el.start();
		}
	}
	
	protected abstract EventLoop newEventLoop(String threadName,int eventQueueSize);

	@Override
	protected void doStop() throws Exception {
		for(EventLoop el : eventLoopArray){
			LifeCycleUtil.stop(el);
		}
	}
}

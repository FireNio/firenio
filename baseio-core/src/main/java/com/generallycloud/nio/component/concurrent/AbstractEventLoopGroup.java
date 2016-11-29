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

	public EventLoop getNext() {
		return eventLoopArray[eventLoopIndex.getAndIncrement()];
	}

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

	protected void doStop() throws Exception {
		for(EventLoop el : eventLoopArray){
			LifeCycleUtil.stop(el);
		}
	}
}

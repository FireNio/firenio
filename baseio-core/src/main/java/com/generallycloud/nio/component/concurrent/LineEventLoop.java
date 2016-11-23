package com.generallycloud.nio.component.concurrent;

import java.util.concurrent.RejectedExecutionException;

import com.generallycloud.nio.AbstractLifeCycle;

public class LineEventLoop extends AbstractLifeCycle implements EventLoop{

	public void dispatch(Runnable job) throws RejectedExecutionException {
		
		if (job == null) {
			return ;
		}
		job.run();
	}

	
	public boolean inEventLoop() {
		return Thread.currentThread() == null; //FIXME 
	}

	
	protected void doStart() throws Exception {
		
	}

	protected void doStop() throws Exception {
		
	}
	
}

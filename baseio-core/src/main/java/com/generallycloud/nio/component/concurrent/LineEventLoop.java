package com.generallycloud.nio.component.concurrent;

import java.util.concurrent.RejectedExecutionException;

import com.generallycloud.nio.AbstractLifeCycle;

public class LineEventLoop extends AbstractLifeCycle implements EventLoop{
	
	private Thread monitor;

	@Override
	public void dispatch(Runnable job) throws RejectedExecutionException {
		
		if (job == null) {
			return ;
		}
		job.run();
	}

	
	@Override
	public boolean inEventLoop() {
		return Thread.currentThread() == monitor; //FIXME 
	}
	
	public Thread getMonitor() {
		return monitor;
	}

	public void setMonitor(Thread monitor) {
		this.monitor = monitor;
	}

	@Override
	protected void doStart() throws Exception {
		
	}

	@Override
	protected void doStop() throws Exception {
		
	}
	
}

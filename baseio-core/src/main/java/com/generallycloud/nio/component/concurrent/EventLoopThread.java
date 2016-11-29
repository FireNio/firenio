package com.generallycloud.nio.component.concurrent;

import com.generallycloud.nio.Looper;

public interface EventLoopThread extends Looper {

	public abstract boolean isMonitor(Thread thread);

	public abstract Thread getMonitor();
	
	public abstract boolean isRunning();
	
	public abstract boolean isStopping();
	
	public abstract void startup(String threadName) throws Exception;

}

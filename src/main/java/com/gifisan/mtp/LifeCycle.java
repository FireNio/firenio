package com.gifisan.mtp;


public interface LifeCycle {
	
	public static int STARTING = 1;
	
	public static int RUNNING  = 2;
	
	public static int STOPPING = 3;
	
	public static int STOPPED  = 4;
	
	public abstract void start() throws Exception;

	public abstract void stop() throws Exception;

	public abstract boolean isRunning();

	public abstract boolean isStarting();

	public abstract boolean isStopping();

	public abstract boolean isStopped();

	public abstract boolean isFailed();

	public abstract void addLifeCycleListener(LifeCycleListener listener);

	public abstract void removeLifeCycleListener(LifeCycleListener listener);

}

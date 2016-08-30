package com.generallycloud.nio;

public interface LifeCycle {

	public static int	STARTING	= 1;

	public static int	RUNNING	= 2;

	public static int	STOPPING	= 3;

	public static int	STOPPED	= 4;

	public abstract void start() throws Exception;

	public abstract void stop();

	public abstract boolean isFailed();

	public abstract boolean isRunning();

	public abstract boolean isStarted();

	public abstract boolean isStarting();

	public abstract boolean isStopped();

	public abstract boolean isStopping();

	public abstract void removeLifeCycleListener(LifeCycleListener listener);

	public abstract void addLifeCycleListener(LifeCycleListener listener);

}

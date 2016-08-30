package com.generallycloud.nio;

import java.util.EventListener;

public interface LifeCycleListener extends EventListener {
	
	public abstract int  lifeCycleListenerSortIndex();
	
	public abstract void lifeCycleStarting(LifeCycle lifeCycle);

	public abstract void lifeCycleStarted(LifeCycle lifeCycle);

	public abstract void lifeCycleFailure(LifeCycle lifeCycle, Exception exception);

	public abstract void lifeCycleStopping(LifeCycle lifeCycle);

	public abstract void lifeCycleStopped(LifeCycle lifeCycle);
	
	
}
package com.generallycloud.nio;

public class AbstractLifeCycleListener implements LifeCycleListener{

	@Override
	public int lifeCycleListenerSortIndex() {
		return 0;
	}

	@Override
	public void lifeCycleStarting(LifeCycle lifeCycle) {
		
	}

	@Override
	public void lifeCycleStarted(LifeCycle lifeCycle) {
		
	}

	@Override
	public void lifeCycleFailure(LifeCycle lifeCycle, Exception exception) {
		
	}

	@Override
	public void lifeCycleStopping(LifeCycle lifeCycle) {
		
	}

	@Override
	public void lifeCycleStopped(LifeCycle lifeCycle) {
		
	}

	
	
}

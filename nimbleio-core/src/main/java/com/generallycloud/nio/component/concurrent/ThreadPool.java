package com.generallycloud.nio.component.concurrent;

import com.generallycloud.nio.LifeCycle;


public interface ThreadPool extends LifeCycle{

	public abstract void dispatch(Runnable job);
	
	

}
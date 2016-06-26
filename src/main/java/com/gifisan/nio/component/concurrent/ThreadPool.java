package com.gifisan.nio.component.concurrent;

import com.gifisan.nio.LifeCycle;


public interface ThreadPool extends LifeCycle{

	public abstract void dispatch(Runnable job);
	
	

}
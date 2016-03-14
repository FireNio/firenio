package com.gifisan.nio.concurrent;

import com.gifisan.nio.LifeCycle;


public interface ThreadPool extends LifeCycle{

	public abstract void dispatch(Runnable job);
	
	

}
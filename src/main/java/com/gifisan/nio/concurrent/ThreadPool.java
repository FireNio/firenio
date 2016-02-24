package com.gifisan.nio.concurrent;

import com.gifisan.nio.LifeCycle;
import com.gifisan.nio.schedule.Job;


public interface ThreadPool extends LifeCycle{

	public abstract void dispatch(Job job);
	
	

}
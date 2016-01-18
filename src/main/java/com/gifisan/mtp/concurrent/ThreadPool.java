package com.gifisan.mtp.concurrent;

import com.gifisan.mtp.LifeCycle;
import com.gifisan.mtp.schedule.Job;


public interface ThreadPool extends LifeCycle{

	public abstract void dispatch(Job job);
	
	

}
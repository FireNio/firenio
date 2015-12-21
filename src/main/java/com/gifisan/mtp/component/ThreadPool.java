package com.gifisan.mtp.component;

import com.gifisan.mtp.LifeCycle;
import com.gifisan.mtp.schedule.Job;


public interface ThreadPool extends LifeCycle{

	public abstract void dispatch(Job job);
	
	

}
package com.gifisan.mtp.component;

import com.gifisan.mtp.schedule.Job;


public interface ThreadPool {

	public abstract void dispatch(Job job);

}
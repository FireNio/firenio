package com.yoocent.mtp.component;

import com.yoocent.mtp.schedule.Job;

public interface ThreadPool {

	public abstract void dispatch(Job job);

}
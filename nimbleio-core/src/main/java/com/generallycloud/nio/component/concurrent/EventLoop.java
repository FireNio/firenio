package com.generallycloud.nio.component.concurrent;

import java.util.concurrent.RejectedExecutionException;

import com.generallycloud.nio.LifeCycle;


public interface EventLoop extends LifeCycle{

	public abstract void dispatch(Runnable job) throws RejectedExecutionException;
}
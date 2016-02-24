package com.gifisan.nio.servlet;

import com.gifisan.nio.LifeCycle;

public interface FilterLoader extends DeployProcess, LifeCycle {

	public abstract NIOFilterWrapper getRootFilter();
	
}
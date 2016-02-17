package com.gifisan.mtp.servlet;

import com.gifisan.mtp.LifeCycle;

public interface FilterLoader extends DeployProcess, LifeCycle {

	public abstract MTPFilterWrapper getRootFilter();
	
}
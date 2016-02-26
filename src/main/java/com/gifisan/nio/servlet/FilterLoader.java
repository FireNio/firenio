package com.gifisan.nio.servlet;

import com.gifisan.nio.LifeCycle;
import com.gifisan.nio.component.HotDeploy;

public interface FilterLoader extends HotDeploy, LifeCycle {

	public abstract NIOFilterWrapper getRootFilter();
	
}
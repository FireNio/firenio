package com.gifisan.mtp.servlet;

import com.gifisan.mtp.LifeCycle;
import com.gifisan.mtp.component.DynamicClassLoader;

public interface FilterLoader extends LifeCycle {

	public abstract boolean redeploy(DynamicClassLoader classLoader);

	public abstract MTPFilterWrapper getRootFilter();

}
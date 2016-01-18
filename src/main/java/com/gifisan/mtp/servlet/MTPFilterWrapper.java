package com.gifisan.mtp.servlet;

import com.gifisan.mtp.LifeCycle;

public interface MTPFilterWrapper extends LifeCycle , MTPFilter{

	public abstract MTPFilterWrapper nextFilter();
	
	public abstract void setNextFilter(MTPFilterWrapper filter);
	
}

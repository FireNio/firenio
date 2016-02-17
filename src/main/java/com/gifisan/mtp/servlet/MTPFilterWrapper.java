package com.gifisan.mtp.servlet;

import com.gifisan.mtp.LifeCycle;
import com.gifisan.mtp.component.Configuration;

public interface MTPFilterWrapper extends LifeCycle , MTPFilter{

	public abstract MTPFilterWrapper nextFilter();
	
	public abstract void setNextFilter(MTPFilterWrapper filter);
	
	public abstract Configuration getConfig();
	
}

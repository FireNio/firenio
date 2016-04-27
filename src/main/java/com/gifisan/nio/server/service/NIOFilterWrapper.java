package com.gifisan.nio.server.service;

import com.gifisan.nio.LifeCycle;
import com.gifisan.nio.component.Configuration;

public interface NIOFilterWrapper extends LifeCycle , NIOFilter{

	public abstract NIOFilterWrapper nextFilter();
	
	public abstract void setNextFilter(NIOFilterWrapper filter);
	
	public abstract Configuration getConfig();
	
}

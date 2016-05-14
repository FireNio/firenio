package com.gifisan.nio.server.service;

import com.gifisan.nio.component.Configuration;

public interface NIOFilterWrapper extends NIOFilter{

	public abstract NIOFilterWrapper nextFilter();
	
	public abstract void setNextFilter(NIOFilterWrapper filter);
	
	public abstract Configuration getConfig();
	
}

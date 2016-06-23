package com.gifisan.nio.server.service;

import com.gifisan.nio.component.Configuration;

public interface ReadFutureAcceptorFilterWrapper extends ReadFutureAcceptorFilter{

	public abstract ReadFutureAcceptorFilterWrapper nextFilter();
	
	public abstract void setNextFilter(ReadFutureAcceptorFilterWrapper filter);
	
	public abstract Configuration getConfig();
	
}

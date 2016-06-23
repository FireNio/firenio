package com.gifisan.nio.server.service;

import com.gifisan.nio.LifeCycle;
import com.gifisan.nio.component.HotDeploy;

public interface FutureAcceptorFilterLoader extends HotDeploy, LifeCycle {

	public abstract ReadFutureAcceptorFilterWrapper getRootFilter();
	
}
package com.gifisan.nio.component;

import java.util.List;
import java.util.Map;

import com.gifisan.nio.server.service.FutureAcceptorFilter;
import com.gifisan.nio.server.service.FutureAcceptorService;

public interface PluginContext extends HotDeploy, Initializeable{

	public abstract void configFutureAcceptorFilter(List<FutureAcceptorFilter> filters);
	
	public abstract void configFutureAcceptor(Map<String, FutureAcceptorService> acceptors);
	
	public abstract int getPluginIndex();

}

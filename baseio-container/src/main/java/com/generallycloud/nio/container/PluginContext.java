package com.generallycloud.nio.container;

import java.util.List;
import java.util.Map;

import com.generallycloud.nio.container.service.FutureAcceptorFilter;
import com.generallycloud.nio.container.service.FutureAcceptorService;

public interface PluginContext extends HotDeploy, Initializeable{

	public abstract void configFutureAcceptorFilter(List<FutureAcceptorFilter> filters);
	
	public abstract void configFutureAcceptor(Map<String, FutureAcceptorService> acceptors);
	
	public abstract int getPluginIndex();

}

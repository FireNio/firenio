package com.gifisan.nio.component;

import java.util.List;
import java.util.Map;

import com.gifisan.nio.server.service.GenericReadFutureAcceptor;
import com.gifisan.nio.server.service.ReadFutureAcceptorFilter;

public interface PluginContext extends HotDeploy, Initializeable{

	public abstract void configFutureAcceptorFilter(List<ReadFutureAcceptorFilter> filters);
	
	public abstract void configFutureAcceptor(Map<String, GenericReadFutureAcceptor> acceptors);
	
	public abstract int getPluginIndex();

}

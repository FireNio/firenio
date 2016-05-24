package com.gifisan.nio.component;

import java.util.List;
import java.util.Map;

import com.gifisan.nio.server.service.GenericServlet;
import com.gifisan.nio.server.service.NIOFilter;

public interface PluginContext extends HotDeploy, Initializeable{

	public abstract void configFilter(List<NIOFilter> pluginFilters);
	
	public abstract void configServlet(Map<String, GenericServlet> servlets);
	
	public abstract int getPluginIndex();

}

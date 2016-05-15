package com.gifisan.nio.server;

import com.gifisan.nio.component.LoginCenter;
import com.gifisan.nio.component.PluginContext;
import com.gifisan.nio.server.configuration.ApplicationConfiguration;
import com.gifisan.nio.server.configuration.ServerConfiguration;
import com.gifisan.nio.server.service.FilterService;

public interface ServerContext extends NIOContext{

	public abstract String getAppLocalAddress();
	
	public abstract FilterService getFilterService();

	public abstract NIOServer getServer();
	
	public abstract boolean redeploy();

	public abstract LoginCenter getLoginCenter();
	
	public abstract PluginContext getPluginContext(Class clazz);
	
	public abstract ApplicationConfiguration getConfiguration() ;
	
	public abstract ServerConfiguration getServerConfiguration() ;
	
}

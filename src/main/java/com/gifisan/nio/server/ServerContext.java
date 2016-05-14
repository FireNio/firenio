package com.gifisan.nio.server;

import com.gifisan.nio.component.LoginCenter;
import com.gifisan.nio.component.PluginContext;
import com.gifisan.nio.server.service.FilterService;

public interface ServerContext extends NIOContext{

	public abstract String getAppLocalAddress();
	
	public abstract FilterService getFilterService();

	public abstract NIOServer getServer();
	
	public abstract int getServerCoreSize();
	
	public abstract int getServerPort();

	public abstract boolean redeploy();

	public abstract void setServerCoreSize(int serverCoreSize);

	public abstract void setServerPort(int serverPort);

	public abstract LoginCenter getLoginCenter();
	
	public abstract PluginContext getPluginContext(Class clazz);
	
}

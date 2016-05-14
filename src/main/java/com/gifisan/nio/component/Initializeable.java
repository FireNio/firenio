package com.gifisan.nio.component;

import com.gifisan.nio.server.ServerContext;

public interface Initializeable {

	public Configuration getConfig();

	public void setConfig(Configuration config);

	public abstract void initialize(ServerContext context, Configuration config) throws Exception;

	public abstract void destroy(ServerContext context, Configuration config) throws Exception;
	
}

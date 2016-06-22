package com.gifisan.nio.component;

import com.gifisan.nio.server.NIOContext;

public interface Initializeable {

	public Configuration getConfig();

	public void setConfig(Configuration config);

	public abstract void initialize(NIOContext context, Configuration config) throws Exception;

	public abstract void destroy(NIOContext context, Configuration config) throws Exception;
	
}

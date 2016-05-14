package com.gifisan.nio.server.service;

import com.gifisan.nio.component.Configuration;
import com.gifisan.nio.component.InitializeableImpl;
import com.gifisan.nio.server.ServerContext;

public abstract class AbstractNIOFilter extends InitializeableImpl implements NIOFilter{

	public void initialize(ServerContext context, Configuration config) throws Exception {
		
	}

	public void destroy(ServerContext context, Configuration config) throws Exception {
		
	}
	
	public void prepare(ServerContext context, Configuration config) throws Exception {
		this.initialize(context, config);
	}

	public void unload(ServerContext context, Configuration config) throws Exception {
		this.destroy(context, config);
	}

}

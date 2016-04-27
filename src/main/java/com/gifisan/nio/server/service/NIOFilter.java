package com.gifisan.nio.server.service;

import com.gifisan.nio.component.Configuration;
import com.gifisan.nio.component.HotDeploy;
import com.gifisan.nio.server.FilterAcceptor;
import com.gifisan.nio.server.ServerContext;

public interface NIOFilter extends HotDeploy, FilterAcceptor {

	public abstract void initialize(ServerContext context, Configuration config) throws Exception;

	public abstract void destroy(ServerContext context, Configuration config) throws Exception;
	
}

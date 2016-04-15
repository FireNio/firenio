package com.gifisan.nio.service;

import com.gifisan.nio.component.Configuration;
import com.gifisan.nio.component.HotDeploy;
import com.gifisan.nio.server.ServerContext;

public interface NIOFilter extends HotDeploy, ServiceAcceptor {

	public abstract void initialize(ServerContext context, Configuration config) throws Exception;

	public abstract void destroy(ServerContext context, Configuration config) throws Exception;
}

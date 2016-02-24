package com.gifisan.nio.servlet;

import com.gifisan.nio.component.Configuration;
import com.gifisan.nio.component.HotDeploy;
import com.gifisan.nio.server.ServerContext;
import com.gifisan.nio.server.ServletAcceptor;

public interface NIOFilter extends HotDeploy, ServletAcceptor {

	public abstract void initialize(ServerContext context, Configuration config) throws Exception;

	public abstract void destroy(ServerContext context, Configuration config) throws Exception;
}

package com.gifisan.nio.servlet;

import com.gifisan.nio.component.Configuration;
import com.gifisan.nio.component.HotDeploy;
import com.gifisan.nio.server.ServerContext;
import com.gifisan.nio.server.ServiceAcceptor;

public abstract class GenericServlet implements HotDeploy, ServiceAcceptor {

	private Configuration	config	= null;

	public Configuration getConfig() {
		return this.config;
	}

	public void setConfig(Configuration config) {
		this.config = config;
	}

	public abstract void initialize(ServerContext context, Configuration config) throws Exception;

	public abstract void destroy(ServerContext context, Configuration config) throws Exception;

}

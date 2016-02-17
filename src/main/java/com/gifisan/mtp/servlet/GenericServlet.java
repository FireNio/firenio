package com.gifisan.mtp.servlet;

import com.gifisan.mtp.component.Configuration;
import com.gifisan.mtp.component.HotDeploy;
import com.gifisan.mtp.server.ServerContext;
import com.gifisan.mtp.server.ServletAcceptor;

public abstract class GenericServlet implements HotDeploy, ServletAcceptor {

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

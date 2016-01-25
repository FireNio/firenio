package com.gifisan.mtp.servlet;

import com.gifisan.mtp.component.ServletConfig;
import com.gifisan.mtp.server.ServerContext;
import com.gifisan.mtp.server.ServletAcceptor;

public abstract class GenericServlet implements ServletAcceptor {

	private ServletConfig	config	= null;

	public ServletConfig getConfig() {
		return this.config;
	}

	public void setConfig(ServletConfig config) {
		this.config = config;
	}

	public abstract void initialize(ServerContext context, ServletConfig config) throws Exception;

	public abstract void destroy(ServerContext context, ServletConfig config) throws Exception;

}

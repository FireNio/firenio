package com.gifisan.mtp.servlet;

import com.gifisan.mtp.component.FilterConfig;
import com.gifisan.mtp.server.ServerContext;
import com.gifisan.mtp.server.ServletAcceptor;

public interface MTPFilter extends ServletAcceptor {

	public abstract void initialize(ServerContext context, FilterConfig config) throws Exception;

	public abstract void destroy(ServerContext context, FilterConfig config) throws Exception;
}

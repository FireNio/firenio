package com.gifisan.mtp.servlet;

import com.gifisan.mtp.component.HotDeploy;
import com.gifisan.mtp.component.Configuration;
import com.gifisan.mtp.server.ServerContext;
import com.gifisan.mtp.server.ServletAcceptor;

public interface MTPFilter extends HotDeploy, ServletAcceptor {

	public abstract void initialize(ServerContext context, Configuration config) throws Exception;

	public abstract void destroy(ServerContext context, Configuration config) throws Exception;
}

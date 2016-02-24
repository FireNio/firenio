package com.gifisan.nio.servlet;

import com.gifisan.nio.LifeCycle;
import com.gifisan.nio.server.ServletAcceptor;

public interface ServletLoader extends DeployProcess, LifeCycle {
	
	public abstract ServletAcceptor getServlet(String serviceName);

}
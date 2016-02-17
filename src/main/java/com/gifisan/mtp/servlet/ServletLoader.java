package com.gifisan.mtp.servlet;

import com.gifisan.mtp.LifeCycle;
import com.gifisan.mtp.server.ServletAcceptor;

public interface ServletLoader extends DeployProcess, LifeCycle {
	
	public abstract ServletAcceptor getServlet(String serviceName);

}
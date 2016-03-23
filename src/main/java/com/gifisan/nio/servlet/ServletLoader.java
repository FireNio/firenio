package com.gifisan.nio.servlet;

import com.gifisan.nio.LifeCycle;
import com.gifisan.nio.component.HotDeploy;
import com.gifisan.nio.server.ServiceAcceptor;

public interface ServletLoader extends HotDeploy, LifeCycle {

	public abstract ServiceAcceptor getServlet(String serviceName);

}
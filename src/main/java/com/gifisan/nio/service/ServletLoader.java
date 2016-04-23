package com.gifisan.nio.service;

import com.gifisan.nio.LifeCycle;
import com.gifisan.nio.component.HotDeploy;
import com.gifisan.nio.server.FilterAcceptor;

public interface ServletLoader extends HotDeploy, LifeCycle {

	public abstract FilterAcceptor getServlet(String serviceName);

}
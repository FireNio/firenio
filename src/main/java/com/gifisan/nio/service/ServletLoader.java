package com.gifisan.nio.service;

import com.gifisan.nio.LifeCycle;
import com.gifisan.nio.component.FilterAcceptor;
import com.gifisan.nio.component.HotDeploy;

public interface ServletLoader extends HotDeploy, LifeCycle {

	public abstract FilterAcceptor getServlet(String serviceName);

}
package com.gifisan.nio.servlet;

import com.gifisan.nio.LifeCycle;
import com.gifisan.nio.component.HotDeploy;
import com.gifisan.nio.server.ServiceAccept;

public interface ServletLoader extends HotDeploy, LifeCycle {

	public abstract ServiceAccept getServlet(String serviceName);

}
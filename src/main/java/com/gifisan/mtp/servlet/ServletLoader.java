package com.gifisan.mtp.servlet;

import com.gifisan.mtp.LifeCycle;
import com.gifisan.mtp.component.DynamicClassLoader;
import com.gifisan.mtp.server.ServletAcceptor;

public interface ServletLoader extends LifeCycle {

	public abstract boolean redeploy(DynamicClassLoader classLoader);

	public abstract ServletAcceptor getServlet(String serviceName);

}
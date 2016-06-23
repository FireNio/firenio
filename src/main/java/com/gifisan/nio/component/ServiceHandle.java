package com.gifisan.nio.component;

import com.gifisan.nio.server.configuration.ApplicationConfiguration;
import com.gifisan.nio.server.service.FutureAcceptorService;
import com.gifisan.security.RoleManager;

public interface ServiceHandle {

	public abstract String getAppLocalAddress();

	public abstract FutureAcceptorService getFilterService();

	public abstract boolean redeploy();

	public abstract LoginCenter getLoginCenter();

	public abstract PluginContext getPluginContext(Class clazz);

	public abstract ApplicationConfiguration getConfiguration();

	public abstract RoleManager getRoleManager();

	public abstract ClassLoader getClassLoader();

}

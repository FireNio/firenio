package com.gifisan.nio.servlet;

import com.gifisan.nio.component.DynamicClassLoader;

public interface DeployProcess {

	public abstract boolean predeploy(DynamicClassLoader classLoader);

	public abstract void redeploy(DynamicClassLoader classLoader);
	
	public abstract void subdeploy(DynamicClassLoader classLoader);
}

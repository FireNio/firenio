package com.gifisan.mtp.servlet;

import com.gifisan.mtp.component.DynamicClassLoader;

public interface DeployProcess {

	public abstract boolean predeploy(DynamicClassLoader classLoader);

	public abstract void redeploy(DynamicClassLoader classLoader);
	
	public abstract void subdeploy(DynamicClassLoader classLoader);
}

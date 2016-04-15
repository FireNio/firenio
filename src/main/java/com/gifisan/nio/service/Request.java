package com.gifisan.nio.service;

import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.server.session.Session;

public interface Request {
	
	public abstract Parameters getParameters();
	
	public abstract String getContent();

	public abstract String getLocalAddr();

	public abstract String getLocalHost();

	public abstract int getLocalPort();

	public abstract int getMaxIdleTime();

	public abstract String getRemoteAddr();

	public abstract String getRemoteHost();
	
	public abstract int getRemotePort();
	
	public abstract String getServiceName();
	
	public abstract Session getSession();
	
	public abstract boolean isBlocking();
	
	public abstract boolean isOpened();
	
}
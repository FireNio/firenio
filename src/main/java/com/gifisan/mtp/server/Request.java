package com.gifisan.mtp.server;

import com.gifisan.mtp.component.ExecutorThreadPool;
import com.gifisan.mtp.component.MTPRequestInputStream;
import com.gifisan.mtp.server.session.Session;

public interface Request {

	public abstract boolean getBooleanParameter(String key);

	public abstract MTPRequestInputStream getInputStream();

	public abstract int getIntegerParameter(String key);

	public abstract String getLocalAddr();

	public abstract String getLocalHost();

	public abstract int getLocalPort();

	public abstract long getLongParameter(String key);

	public abstract int getMaxIdleTime();

	public abstract Object getObjectParameter(String key);

	public abstract String getRemoteAddr();

	public abstract String getRemoteHost();
	
	public abstract int getRemotePort();
	
	public abstract String getServiceName();
	
	public abstract Session getSession();
	
	public abstract String getParameter(String key);
	
	public abstract boolean isBlocking();
	
	public abstract boolean isOpened();
	
	public abstract ExecutorThreadPool getExecutorThreadPool();
	
}
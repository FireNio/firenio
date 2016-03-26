package com.gifisan.nio.server;

import com.gifisan.nio.component.InputStream;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.concurrent.ExecutorThreadPool;
import com.gifisan.nio.server.session.Session;

public interface Request {
	
	public abstract Parameters getParameters();
	
	public abstract String getContent();

	public abstract InputStream getInputStream();

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
	
	public abstract ExecutorThreadPool getExecutorThreadPool();
	
}
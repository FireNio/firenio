package com.gifisan.mtp.server;

import com.gifisan.mtp.component.MTPRequestInputStream;
import com.gifisan.mtp.component.RequestParam;
import com.gifisan.mtp.concurrent.ExecutorThreadPool;
import com.gifisan.mtp.server.session.Session;

public interface Request {
	
	public abstract RequestParam getParameters();
	
	public abstract String getContent();

	public abstract MTPRequestInputStream getInputStream();

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
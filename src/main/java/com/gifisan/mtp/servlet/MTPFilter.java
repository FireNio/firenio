package com.gifisan.mtp.servlet;

import com.gifisan.mtp.component.FilterConfig;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;
import com.gifisan.mtp.server.context.ServletContext;

public interface MTPFilter{
	
	public abstract boolean doFilter(ChainFilter filter,Request request, Response response) throws Exception;
	
	public abstract void initialize(ServletContext context,FilterConfig config) throws Exception;
	
	public abstract void destroy(ServletContext context,FilterConfig config) throws Exception;
}

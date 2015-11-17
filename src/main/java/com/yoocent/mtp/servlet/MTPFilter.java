package com.yoocent.mtp.servlet;

import com.yoocent.mtp.component.FilterConfig;
import com.yoocent.mtp.server.Request;
import com.yoocent.mtp.server.Response;
import com.yoocent.mtp.server.context.ServletContext;

public interface MTPFilter{
	
	public abstract boolean doFilter(ChainFilter filter,Request request, Response response) throws Exception;
	
	public abstract void initialize(ServletContext context,FilterConfig config) throws Exception;
	
	public abstract void destroy(ServletContext context,FilterConfig config) throws Exception;
}

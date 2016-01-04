package com.gifisan.mtp.servlet;

import com.gifisan.mtp.component.FilterConfig;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;
import com.gifisan.mtp.server.ServletContext;

public interface MTPFilter{
	
	boolean CONTINUE = false;
	
	boolean BREAK = true;
	
	/**
	 * 
	 * 返回true结束处理</BR>
	 * 返回false继续处理
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public abstract boolean doFilter(Request request, Response response) throws Exception;
	
	public abstract void initialize(ServletContext context,FilterConfig config) throws Exception;
	
	public abstract void destroy(ServletContext context,FilterConfig config) throws Exception;
}

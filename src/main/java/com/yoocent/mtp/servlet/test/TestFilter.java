package com.yoocent.mtp.servlet.test;

import com.yoocent.mtp.component.FilterConfig;
import com.yoocent.mtp.server.Request;
import com.yoocent.mtp.server.Response;
import com.yoocent.mtp.server.context.ServletContext;
import com.yoocent.mtp.servlet.ChainFilter;
import com.yoocent.mtp.servlet.MTPFilter;

public class TestFilter implements MTPFilter{

	public boolean doFilter(ChainFilter filter, Request request,Response response) throws Exception {
		return false;
	}

	public void initialize(ServletContext context, FilterConfig config)
			throws Exception {
		
	}

	public void destroy(ServletContext context, FilterConfig config)
			throws Exception {
		
	}

	
	
}

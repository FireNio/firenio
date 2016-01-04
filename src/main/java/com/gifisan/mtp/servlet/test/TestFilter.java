package com.gifisan.mtp.servlet.test;

import com.gifisan.mtp.component.FilterConfig;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;
import com.gifisan.mtp.server.ServletContext;
import com.gifisan.mtp.servlet.MTPFilter;

public class TestFilter implements MTPFilter{

	public boolean doFilter(Request request,Response response) throws Exception {
		
		
		
		return CONTINUE;
	}

	public void initialize(ServletContext context, FilterConfig config)
			throws Exception {
		
	}

	public void destroy(ServletContext context, FilterConfig config)
			throws Exception {
		
	}

	
	
}

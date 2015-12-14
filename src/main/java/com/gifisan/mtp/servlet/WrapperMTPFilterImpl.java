package com.gifisan.mtp.servlet;

import com.gifisan.mtp.AbstractLifeCycle;
import com.gifisan.mtp.component.FilterConfig;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;
import com.gifisan.mtp.server.context.ServletContext;

public class WrapperMTPFilterImpl extends AbstractLifeCycle implements WrapperMTPFilter{

	private ServletContext context = null;
	
	private MTPFilter filter = null;
	
	private FilterConfig config = null;
	
	public WrapperMTPFilterImpl(ServletContext context,MTPFilter filter, FilterConfig config) {
		this.context = context;
		this.filter = filter;
		this.config = config;
	}

	public boolean doFilter(ChainFilter filter, Request request,Response response) throws Exception {
		return this.filter.doFilter(filter, request, response);
	}

	public void initialize(ServletContext context, FilterConfig config)throws Exception {
		this.filter.initialize(context, config);
		
	}

	public void destroy(ServletContext context, FilterConfig config)throws Exception {
		this.filter.destroy(context, config);
		
	}

	protected void doStart() throws Exception {
		this.initialize(context, config);
		
	}

	protected void doStop() throws Exception {
		this.destroy(context, config);
		
	}
	
	
}

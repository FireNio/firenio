package com.gifisan.mtp.servlet;

import com.gifisan.mtp.AbstractLifeCycle;
import com.gifisan.mtp.component.FilterConfig;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;
import com.gifisan.mtp.server.ServletContext;

public class MTPFilterWrapperImpl extends AbstractLifeCycle implements MTPFilterWrapper{

	private ServletContext context = null;
	
	private MTPFilter filter = null;
	
	private FilterConfig config = null;
	
	public MTPFilterWrapperImpl(ServletContext context,MTPFilter filter, FilterConfig config) {
		this.context = context;
		this.filter = filter;
		this.config = config;
	}

	public boolean doFilter(Request request,Response response) throws Exception {
		return this.filter.doFilter(request, response);
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

	private MTPFilterWrapper next = null;
	
	public MTPFilterWrapper nextFilter() {
		return next;
	}

	public void setNextFilter(MTPFilterWrapper filter) {
		this.next = filter;
		
	}
	
	
}

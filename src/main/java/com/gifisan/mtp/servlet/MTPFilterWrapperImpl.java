package com.gifisan.mtp.servlet;

import com.gifisan.mtp.AbstractLifeCycle;
import com.gifisan.mtp.component.FilterConfig;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;
import com.gifisan.mtp.server.ServerContext;

public class MTPFilterWrapperImpl extends AbstractLifeCycle implements MTPFilterWrapper {

	private FilterConfig	config		= null;
	private ServerContext	context		= null;
	private MTPFilter		filter		= null;
	private MTPFilterWrapper	nextFilter	= null;

	public MTPFilterWrapperImpl(ServerContext context, MTPFilter filter, FilterConfig config) {
		this.context = context;
		this.filter = filter;
		this.config = config;
	}

	public void accept(Request request, Response response) throws Exception {
		this.filter.accept(request, response);
	}

	public void destroy(ServerContext context, FilterConfig config) throws Exception {
		this.filter.destroy(context, config);

	}

	protected void doStart() throws Exception {
		this.initialize(context, config);

	}

	protected void doStop() throws Exception {
		this.destroy(context, config);

	}

	public void initialize(ServerContext context, FilterConfig config) throws Exception {
		this.filter.initialize(context, config);

	}

	public MTPFilterWrapper nextFilter() {
		return nextFilter;
	}

	public void setNextFilter(MTPFilterWrapper filter) {
		this.nextFilter = filter;
	}

	public String toString() {
		return "Warpper(" + this.filter.toString() + ")";
	}

}

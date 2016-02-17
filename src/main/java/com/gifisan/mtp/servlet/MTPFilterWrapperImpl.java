package com.gifisan.mtp.servlet;

import com.gifisan.mtp.AbstractLifeCycle;
import com.gifisan.mtp.component.Configuration;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;
import com.gifisan.mtp.server.ServerContext;

public class MTPFilterWrapperImpl extends AbstractLifeCycle implements MTPFilterWrapper {

	private Configuration	config		= null;
	private ServerContext	context		= null;
	private MTPFilter		filter		= null;
	private MTPFilterWrapper	nextFilter	= null;

	public MTPFilterWrapperImpl(ServerContext context, MTPFilter filter, Configuration config) {
		this.context = context;
		this.filter = filter;
		this.config = config;
	}

	public void accept(Request request, Response response) throws Exception {
		this.filter.accept(request, response);
	}

	public void destroy(ServerContext context, Configuration config) throws Exception {
		this.filter.destroy(context, config);

	}

	protected void doStart() throws Exception {
		this.initialize(context, config);

	}

	protected void doStop() throws Exception {
		this.destroy(context, config);

	}

	public void initialize(ServerContext context, Configuration config) throws Exception {
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

	public void onPreDeploy(ServerContext context, Configuration config) throws Exception {
		filter.onPreDeploy(context, config);
	}

	public void onSubDeploy(ServerContext context, Configuration config) throws Exception {
		filter.onSubDeploy(context, config);
	}

	public Configuration getConfig() {
		return this.config;
	}

}

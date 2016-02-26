package com.gifisan.nio.servlet;

import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.component.Configuration;
import com.gifisan.nio.server.Request;
import com.gifisan.nio.server.Response;
import com.gifisan.nio.server.ServerContext;

public class NIOFilterWrapperImpl extends AbstractLifeCycle implements NIOFilterWrapper {

	private Configuration	config		= null;
	private ServerContext	context		= null;
	private NIOFilter		filter		= null;
	private NIOFilterWrapper	nextFilter	= null;

	public NIOFilterWrapperImpl(ServerContext context, NIOFilter filter, Configuration config) {
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

	public NIOFilterWrapper nextFilter() {
		return nextFilter;
	}

	public void setNextFilter(NIOFilterWrapper filter) {
		this.nextFilter = filter;
	}

	public String toString() {
		return "Warpper(" + this.filter.toString() + ")";
	}
	
	public void prepare(ServerContext context, Configuration config) throws Exception {
		filter.prepare(context, config);
	}

	public void unload(ServerContext context, Configuration config) throws Exception {
		filter.unload(context, config);
		
	}

	public Configuration getConfig() {
		return this.config;
	}

}

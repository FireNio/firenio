package com.gifisan.nio.server.service;

import com.gifisan.nio.component.Configuration;
import com.gifisan.nio.component.InitializeableImpl;
import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.server.IOSession;
import com.gifisan.nio.server.ServerContext;

public class DefaultNIOFilterWrapper extends InitializeableImpl implements NIOFilterWrapper {

	private NIOFilter		filter		= null;
	private NIOFilterWrapper	nextFilter	= null;

	public DefaultNIOFilterWrapper(ServerContext context, NIOFilter filter, Configuration config) {
		this.filter = filter;
		this.setConfig(config);
	}

	public void accept(IOSession session,ServerReadFuture future) throws Exception {
		this.filter.accept(session,future);
	}

	public void destroy(ServerContext context, Configuration config) throws Exception {
		this.filter.destroy(context, config);

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

}

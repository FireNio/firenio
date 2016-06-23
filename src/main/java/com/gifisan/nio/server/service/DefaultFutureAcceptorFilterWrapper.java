package com.gifisan.nio.server.service;

import com.gifisan.nio.component.ApplicationContext;
import com.gifisan.nio.component.Configuration;
import com.gifisan.nio.component.InitializeableImpl;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;

public class DefaultFutureAcceptorFilterWrapper extends InitializeableImpl implements ReadFutureAcceptorFilterWrapper {

	private ReadFutureAcceptorFilter		filter		= null;
	private ReadFutureAcceptorFilterWrapper	nextFilter	= null;

	public DefaultFutureAcceptorFilterWrapper(ApplicationContext context, ReadFutureAcceptorFilter filter, Configuration config) {
		this.filter = filter;
		this.setConfig(config);
	}

	public void accept(Session session,ReadFuture future) throws Exception {
		this.filter.accept(session,future);
	}

	public void destroy(ApplicationContext context, Configuration config) throws Exception {
		this.filter.destroy(context, config);

	}

	public void initialize(ApplicationContext context, Configuration config) throws Exception {
		this.filter.initialize(context, config);
	}

	public ReadFutureAcceptorFilterWrapper nextFilter() {
		return nextFilter;
	}

	public void setNextFilter(ReadFutureAcceptorFilterWrapper filter) {
		this.nextFilter = filter;
	}

	public String toString() {
		return "Warpper(" + this.filter.toString() + ")";
	}
	
	public void prepare(ApplicationContext context, Configuration config) throws Exception {
		filter.prepare(context, config);
	}

	public void unload(ApplicationContext context, Configuration config) throws Exception {
		filter.unload(context, config);
		
	}

}

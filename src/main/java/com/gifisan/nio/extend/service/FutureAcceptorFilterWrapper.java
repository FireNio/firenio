package com.gifisan.nio.extend.service;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.future.ReadFuture;
import com.gifisan.nio.extend.ApplicationContext;
import com.gifisan.nio.extend.configuration.Configuration;

public class FutureAcceptorFilterWrapper extends FutureAcceptorFilter {

	private FutureAcceptorFilter		filter		;
	private FutureAcceptorFilterWrapper	nextFilter	;

	public FutureAcceptorFilterWrapper(ApplicationContext context, FutureAcceptorFilter filter, Configuration config) {
		this.filter = filter;
		this.setConfig(config);
	}

	public void accept(Session session, ReadFuture future) throws Exception {
		this.filter.accept(session,future);
	}
	
	public void exceptionCaught(Session session, ReadFuture future, Exception cause, IOEventState state) {
		this.filter.exceptionCaught(session, future, cause, state);
	}

	public void futureSent(Session session, ReadFuture future) {
		this.filter.futureSent(session, future);
	}

	public void destroy(ApplicationContext context, Configuration config) throws Exception {
		this.filter.destroy(context, config);
	}

	public void initialize(ApplicationContext context, Configuration config) throws Exception {
		this.filter.initialize(context, config);
	}

	public FutureAcceptorFilterWrapper nextFilter() {
		return nextFilter;
	}

	public void setNextFilter(FutureAcceptorFilterWrapper filter) {
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

package com.generallycloud.nio.container.service;

import com.generallycloud.nio.Linkable;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.container.ApplicationContext;
import com.generallycloud.nio.container.configuration.Configuration;
import com.generallycloud.nio.protocol.NamedReadFuture;
import com.generallycloud.nio.protocol.ReadFuture;

public class FutureAcceptorFilterWrapper extends FutureAcceptorFilter implements Linkable<FutureAcceptorFilter> {

	private FutureAcceptorFilter			filter;
	private Linkable<FutureAcceptorFilter>	nextFilter;

	public FutureAcceptorFilterWrapper(ApplicationContext context, FutureAcceptorFilter filter, Configuration config) {
		this.filter = filter;
		this.setConfig(config);
	}

	public void accept(SocketSession session, ReadFuture future) throws Exception {
		this.filter.accept(session, future);
	}

	protected void accept(SocketSession session, NamedReadFuture future) throws Exception {
		this.filter.accept(session, future);
	}

	public void exceptionCaught(SocketSession session, ReadFuture future, Exception cause, IoEventState state) {
		this.filter.exceptionCaught(session, future, cause, state);
	}

	public void futureSent(SocketSession session, ReadFuture future) {
		this.filter.futureSent(session, future);
	}

	public void destroy(ApplicationContext context, Configuration config) throws Exception {
		this.filter.destroy(context, config);
	}

	public void initialize(ApplicationContext context, Configuration config) throws Exception {
		this.filter.initialize(context, config);
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

	public Linkable<FutureAcceptorFilter> getNext() {
		return nextFilter;
	}

	public void setNext(Linkable<FutureAcceptorFilter> next) {
		this.nextFilter = next;
	}

	public FutureAcceptorFilter getValue() {
		return this;
	}

}

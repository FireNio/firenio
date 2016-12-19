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

	@Override
	public void accept(SocketSession session, ReadFuture future) throws Exception {
	
		this.filter.accept(session, future);
		
		if (future.flushed()) {

			return;
		}
		
		nextAccept(session, future);
	}
	
	private void nextAccept(SocketSession session, ReadFuture future) throws Exception{
		
		Linkable<FutureAcceptorFilter> next = getNext();
		
		if (next == null) {
			return;
		}
		
		next.getValue().accept(session, future);
	}

	@Override
	protected void accept(SocketSession session, NamedReadFuture future) throws Exception {
		this.filter.accept(session, future);
	}

	@Override
	public void exceptionCaught(SocketSession session, ReadFuture future, Exception cause, IoEventState state) {
		this.filter.exceptionCaught(session, future, cause, state);
	}

	@Override
	public void futureSent(SocketSession session, ReadFuture future) {
		this.filter.futureSent(session, future);
	}

	@Override
	public void destroy(ApplicationContext context, Configuration config) throws Exception {
		this.filter.destroy(context, config);
	}

	@Override
	public void initialize(ApplicationContext context, Configuration config) throws Exception {
		this.filter.initialize(context, config);
	}

	@Override
	public String toString() {
		return "Warpper(" + this.filter.toString() + ")";
	}

	@Override
	public void prepare(ApplicationContext context, Configuration config) throws Exception {
		filter.prepare(context, config);
	}

	@Override
	public void unload(ApplicationContext context, Configuration config) throws Exception {
		filter.unload(context, config);
	}

	@Override
	public Linkable<FutureAcceptorFilter> getNext() {
		return nextFilter;
	}

	@Override
	public void setNext(Linkable<FutureAcceptorFilter> next) {
		this.nextFilter = next;
	}

	@Override
	public FutureAcceptorFilter getValue() {
		return this;
	}

}

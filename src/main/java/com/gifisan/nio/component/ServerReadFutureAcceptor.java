package com.gifisan.nio.component;

import com.gifisan.nio.concurrent.ThreadPool;
import com.gifisan.nio.server.session.Session;

public class ServerReadFutureAcceptor implements ReadFutureAcceptor {

	private ThreadPool dispatcher = null;
	
	public ServerReadFutureAcceptor(ThreadPool dispatcher) {
		this.dispatcher = dispatcher;
	}

	public void accept(Session session, ReadFuture future) {

		ServerServiceAcceptor acceptor = (ServerServiceAcceptor) session.getServiceAcceptor();
		
		acceptor.update(future);
		
		dispatcher.dispatch(acceptor);
		
	}

}

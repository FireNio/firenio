package com.gifisan.nio.server;

import com.gifisan.nio.component.ReadFuture;
import com.gifisan.nio.component.ReadFutureAcceptor;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.concurrent.ThreadPool;

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

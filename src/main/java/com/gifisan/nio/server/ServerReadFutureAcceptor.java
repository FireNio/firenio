package com.gifisan.nio.server;

import com.gifisan.nio.component.IOReadFuture;
import com.gifisan.nio.component.ReadFutureAcceptor;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.concurrent.ThreadPool;
import com.gifisan.nio.server.session.ServerSession;

public class ServerReadFutureAcceptor implements ReadFutureAcceptor {

	private ThreadPool dispatcher = null;
	
	public ServerReadFutureAcceptor(ThreadPool dispatcher) {
		this.dispatcher = dispatcher;
	}

	public void accept(Session session, IOReadFuture future) {

		ServerServiceAcceptor acceptor = ((ServerSession)session).getServiceAcceptor();
		
		acceptor.update(future);
		
		dispatcher.dispatch(acceptor);
		
	}

}

package com.gifisan.nio.component;

import com.gifisan.nio.concurrent.ThreadPool;
import com.gifisan.nio.server.selector.ServiceAcceptorJob;
import com.gifisan.nio.server.session.IOSession;
import com.gifisan.nio.server.session.Session;

public class ServerReadFutureAcceptor implements ReadFutureAcceptor {

	private ThreadPool dispatcher = null;
	
	public ServerReadFutureAcceptor(ThreadPool dispatcher) {
		this.dispatcher = dispatcher;
	}

	public void accept(Session session, ReadFuture future) {

		ServiceAcceptorJob acceptorJob = ((IOSession) session).getServiceAcceptorJob();
		
		acceptorJob.update(future);
		
		dispatcher.dispatch(acceptorJob);
		
	}

}

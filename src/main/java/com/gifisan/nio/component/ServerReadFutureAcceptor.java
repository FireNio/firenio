package com.gifisan.nio.component;

import com.gifisan.nio.concurrent.ThreadPool;
import com.gifisan.nio.server.selector.ServiceAcceptorJob;
import com.gifisan.nio.server.session.Session;

public class ServerReadFutureAcceptor implements ReadFutureAcceptor {

	private ThreadPool dispatcher = null;
	
	public void accept(Session session, IOReadFuture future) {

		ServiceAcceptorJob acceptorJob = session.getServiceAcceptorJob();
		
		acceptorJob.update(future);
		
		dispatcher.dispatch(acceptorJob);
		
	}

}

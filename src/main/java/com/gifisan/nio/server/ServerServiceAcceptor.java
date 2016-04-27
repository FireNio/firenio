package com.gifisan.nio.server;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.ServiceAcceptor;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.IOReadFuture;
import com.gifisan.nio.server.session.ServerSession;
import com.gifisan.nio.service.FilterService;

public class ServerServiceAcceptor implements ServiceAcceptor,Runnable {

	private Logger			logger	= LoggerFactory.getLogger(ServerServiceAcceptor.class);
	private FilterService	service	= null;
	private ServerSession	session	= null;
	private IOReadFuture	future	= null;

	public ServerServiceAcceptor(ServerSession session, IOReadFuture future) {
		this.session = session;
		this.future = future;
		this.service = session.getContext().getFilterService();
	}
	
	public ServerServiceAcceptor(ServerContext context) {
		this.service = context.getFilterService();
	}

	public void run() {
		this.accept(session, future);
	}

	public void accept(Session session, IOReadFuture future) {
		try {
			this.service.accept(session, future);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

}

package com.gifisan.nio.component;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.server.session.IOSession;
import com.gifisan.nio.server.session.Session;
import com.gifisan.nio.service.FilterService;
import com.gifisan.nio.service.ServiceAcceptor;

public class ServerServiceAcceptor implements ServiceAcceptor,Runnable {

	private Logger			logger	= LoggerFactory.getLogger(ServerServiceAcceptor.class);
	private FilterService	service	= null;
	private IOSession		session	= null;
	private ReadFuture		future	= null;

	public ServerServiceAcceptor(Session session, FilterService service) {
		this.session = (IOSession) session;
		this.service = service;
	}

	public void run() {
		this.accept(session, future);
	}

	public void accept(Session session, ReadFuture future) {
		try {
			this.service.accept(this.session, future);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void update(ReadFuture future) {
		this.future = future;
	}
}

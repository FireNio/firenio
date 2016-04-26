package com.gifisan.nio.server;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.IOReadFuture;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.service.FilterService;
import com.gifisan.nio.service.ServiceAcceptor;

public class ServerServiceAcceptor implements ServiceAcceptor,Runnable {

	private Logger			logger	= LoggerFactory.getLogger(ServerServiceAcceptor.class);
	private FilterService	service	= null;
	private Session		session	= null;
	private IOReadFuture	future	= null;

	public ServerServiceAcceptor(Session session, FilterService service) {
		this.session = session;
		this.service = service;
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

	public void update(IOReadFuture future) {
		this.future = future;
	}
}

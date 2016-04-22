package com.gifisan.nio.component;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.server.selector.ServiceAcceptorJob;
import com.gifisan.nio.server.session.Session;
import com.gifisan.nio.service.FilterService;

public class NormalServiceAcceptor implements ServiceAcceptorJob {

	private Logger			logger	= LoggerFactory.getLogger(NormalServiceAcceptor.class);
	private FilterService	service	= null;
	private Session		session	= null;
	private ReadFuture		future	= null;

	public NormalServiceAcceptor(Session session, FilterService service) {
		this.session = session;
		this.service = service;
	}

	public void run() {
		this.accept(session, future);
	}

	public void accept(Session session, ReadFuture future) {
		try {
			this.service.accept(session, future);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void update(ReadFuture future) {
		this.future = future;
	}
}

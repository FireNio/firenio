package com.gifisan.nio.extend.plugin.rtp.server;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.nio.future.NIOReadFuture;
import com.gifisan.nio.extend.service.NIOFutureAcceptorService;

public abstract class RTPServlet extends NIOFutureAcceptorService {

	private RTPContext	context	= RTPContext.getInstance();

	public RTPContext getRTPContext() {
		return context;
	}

	public void doAccept(Session session, NIOReadFuture future) throws Exception {

		RTPSessionAttachment attachment = context.getSessionAttachment(session);

		this.doAccept(session, future, attachment);
	}

	public abstract void doAccept(Session session, NIOReadFuture future, RTPSessionAttachment attachment)
			throws Exception;

}

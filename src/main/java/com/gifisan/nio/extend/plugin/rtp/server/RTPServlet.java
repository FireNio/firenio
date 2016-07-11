package com.gifisan.nio.extend.plugin.rtp.server;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.nio.NIOReadFuture;
import com.gifisan.nio.extend.service.FutureAcceptorService;

public abstract class RTPServlet extends FutureAcceptorService {

	private RTPContext	context	= RTPContext.getInstance();

	public RTPContext getRTPContext() {
		return context;
	}

	public void doAccept(Session session, NIOReadFuture future) throws Exception {

		RTPSessionAttachment attachment = context.getSessionAttachment(session);

		this.accept(session, future, attachment);
	}

	public abstract void accept(Session session, NIOReadFuture future, RTPSessionAttachment attachment)
			throws Exception;

}

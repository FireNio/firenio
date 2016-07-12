package com.gifisan.nio.extend.plugin.jms.server;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.nio.future.NIOReadFuture;
import com.gifisan.nio.extend.service.FutureAcceptorService;

public abstract class MQServlet extends FutureAcceptorService {

	private MQContext	context	= MQContext.getInstance();

	public MQContext getMQContext() {
		return context;
	}

	protected void doAccept(Session session, NIOReadFuture future) throws Exception {

		MQSessionAttachment attachment = context.getSessionAttachment(session);

		this.accept(session, future, attachment);
	}

	public abstract void accept(Session session, NIOReadFuture future, MQSessionAttachment attachment)
			throws Exception;

}

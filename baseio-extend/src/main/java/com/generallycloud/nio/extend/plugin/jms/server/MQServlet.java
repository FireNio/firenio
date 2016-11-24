package com.generallycloud.nio.extend.plugin.jms.server;

import com.generallycloud.nio.codec.base.future.BaseReadFuture;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.extend.service.BaseFutureAcceptorService;

public abstract class MQServlet extends BaseFutureAcceptorService {

	private MQContext	context	= MQContext.getInstance();

	public MQContext getMQContext() {
		return context;
	}

	public void doAccept(SocketSession session, BaseReadFuture future) throws Exception {

		MQSessionAttachment attachment = context.getSessionAttachment(session);

		this.doAccept(session, future, attachment);
	}

	public abstract void doAccept(SocketSession session, BaseReadFuture future, MQSessionAttachment attachment)
			throws Exception;

}

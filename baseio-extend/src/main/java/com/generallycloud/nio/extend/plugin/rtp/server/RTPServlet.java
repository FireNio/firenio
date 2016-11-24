package com.generallycloud.nio.extend.plugin.rtp.server;

import com.generallycloud.nio.codec.base.future.BaseReadFuture;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.extend.service.BaseFutureAcceptorService;

public abstract class RTPServlet extends BaseFutureAcceptorService {

	private RTPContext	context	= RTPContext.getInstance();

	public RTPContext getRTPContext() {
		return context;
	}

	public void doAccept(SocketSession session, BaseReadFuture future) throws Exception {

		RTPSessionAttachment attachment = context.getSessionAttachment(session);

		this.doAccept(session, future, attachment);
	}

	public abstract void doAccept(SocketSession session, BaseReadFuture future, RTPSessionAttachment attachment)
			throws Exception;

}

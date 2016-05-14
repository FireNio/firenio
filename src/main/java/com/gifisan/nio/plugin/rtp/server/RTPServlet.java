package com.gifisan.nio.plugin.rtp.server;

import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.server.IOSession;
import com.gifisan.nio.server.service.NIOServlet;

public abstract class RTPServlet extends NIOServlet{

	private RTPContext context = RTPContextFactory.getRTPContext();

	public RTPContext getRTPContext() {
		return context;
	}

	public void accept(IOSession session,ServerReadFuture future) throws Exception {
		this.accept(session, future,(RTPSessionAttachment) session.getAttachment(context));
	}
	
	public abstract void accept(IOSession session,ServerReadFuture future,RTPSessionAttachment attachment) throws Exception;
	
}

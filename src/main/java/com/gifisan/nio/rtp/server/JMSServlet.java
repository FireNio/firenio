package com.gifisan.nio.rtp.server;

import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.server.IOSession;
import com.gifisan.nio.server.service.NIOServlet;

public abstract class JMSServlet extends NIOServlet{

	private RTPContext context = RTPContextFactory.getMQContext();

	public RTPContext getRTPContext() {
		return context;
	}

	public void accept(IOSession session,ServerReadFuture future) throws Exception {
		this.accept(session, future,(RTPSessionAttachment) session.attachment());
	}
	
	public abstract void accept(IOSession session,ServerReadFuture future,RTPSessionAttachment attachment) throws Exception;
	
}

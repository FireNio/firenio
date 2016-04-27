package com.gifisan.nio.jms.server;

import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.server.service.NIOServlet;
import com.gifisan.nio.server.session.IOSession;

public abstract class JMSServlet extends NIOServlet{

	private MQContext context = MQContextFactory.getMQContext();

	public MQContext getMQContext() {
		return context;
	}

	public void accept(IOSession session,ServerReadFuture future) throws Exception {
		this.accept(session, future,(JMSSessionAttachment) session.attachment());
	}
	
	public abstract void accept(IOSession session,ServerReadFuture future,JMSSessionAttachment attachment) throws Exception;
	
}

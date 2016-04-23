package com.gifisan.nio.jms.server;

import com.gifisan.nio.component.ReadFuture;
import com.gifisan.nio.server.session.IOSession;
import com.gifisan.nio.service.NIOServlet;

public abstract class JMSServlet extends NIOServlet{

	private MQContext context = MQContextFactory.getMQContext();

	public MQContext getMQContext() {
		return context;
	}

	public void accept(IOSession session,ReadFuture future) throws Exception {
		this.accept(session, future,(JMSSessionAttachment) session.attachment());
	}
	
	public abstract void accept(IOSession session,ReadFuture future,JMSSessionAttachment attachment) throws Exception;
	
}

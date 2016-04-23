package com.gifisan.nio.jms.server;

import com.gifisan.nio.server.session.ServerSession;
import com.gifisan.nio.service.NIOServlet;

public abstract class JMSServlet extends NIOServlet{

	private MQContext context = MQContextFactory.getMQContext();

	public MQContext getMQContext() {
		return context;
	}

	public void accept(ServerSession session) throws Exception {
		this.accept(session, (JMSSessionAttachment) session.attachment());
	}
	
	public abstract void accept(ServerSession session,JMSSessionAttachment attachment) throws Exception;
	
}

package com.gifisan.nio.jms.server;

import com.gifisan.nio.server.session.NIOSession;
import com.gifisan.nio.service.NIOServlet;

public abstract class JMSServlet extends NIOServlet{

	private MQContext context = MQContextFactory.getMQContext();

	public MQContext getMQContext() {
		return context;
	}

	public void accept(NIOSession session) throws Exception {
		this.accept(session, (JMSSessionAttachment) session.attachment());
	}
	
	public abstract void accept(NIOSession session,JMSSessionAttachment attachment) throws Exception;
	
}

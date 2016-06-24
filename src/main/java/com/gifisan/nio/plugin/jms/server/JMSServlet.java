package com.gifisan.nio.plugin.jms.server;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.server.service.FutureAcceptorService;

public abstract class JMSServlet extends FutureAcceptorService{

	private MQContext context = MQContext.getInstance();

	public MQContext getMQContext() {
		return context;
	}

	public void accept(Session session,ReadFuture future) throws Exception {
		
		JMSSessionAttachment attachment = context.getSessionAttachment(session);
		
		this.accept(session, future,attachment);
	}
	
	public abstract void accept(Session session,ReadFuture future,JMSSessionAttachment attachment) throws Exception;
	
}

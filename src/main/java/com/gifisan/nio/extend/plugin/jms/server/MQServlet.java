package com.gifisan.nio.extend.plugin.jms.server;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.extend.service.FutureAcceptorService;

public abstract class MQServlet extends FutureAcceptorService{

	private MQContext context = MQContext.getInstance();

	public MQContext getMQContext() {
		return context;
	}

	public void accept(Session session,ReadFuture future) throws Exception {
		
		MQSessionAttachment attachment = context.getSessionAttachment(session);
		
		this.accept(session, future,attachment);
	}
	
	public abstract void accept(Session session,ReadFuture future,MQSessionAttachment attachment) throws Exception;
	
}

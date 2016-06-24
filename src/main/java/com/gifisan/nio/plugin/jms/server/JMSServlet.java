package com.gifisan.nio.plugin.jms.server;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.server.service.FutureAcceptorService;

public abstract class JMSServlet extends FutureAcceptorService{

	private MQContext context = MQContextFactory.getMQContext();

	public MQContext getMQContext() {
		return context;
	}

	public void accept(Session session,ReadFuture future) throws Exception {
		
		JMSSessionAttachment attachment = (JMSSessionAttachment) session.getAttachment(context);
		
		if (attachment == null) {

			attachment = new JMSSessionAttachment(context);

			session.setAttachment(context, attachment);
		}
		
		this.accept(session, future,attachment);
	}
	
	public abstract void accept(Session session,ReadFuture future,JMSSessionAttachment attachment) throws Exception;
	
}

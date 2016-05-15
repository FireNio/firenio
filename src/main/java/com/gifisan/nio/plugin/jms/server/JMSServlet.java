package com.gifisan.nio.plugin.jms.server;

import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.server.IOSession;
import com.gifisan.nio.server.service.NIOServlet;

public abstract class JMSServlet extends NIOServlet{

	private MQContext context = MQContextFactory.getMQContext();

	public MQContext getMQContext() {
		return context;
	}

	public void accept(IOSession session,ServerReadFuture future) throws Exception {
		
		JMSSessionAttachment attachment = (JMSSessionAttachment) session.getAttachment(context);
		
		if (attachment == null) {

			attachment = new JMSSessionAttachment(context);

			session.setAttachment(context, attachment);
		}
		
		this.accept(session, future,attachment);
	}
	
	public abstract void accept(IOSession session,ServerReadFuture future,JMSSessionAttachment attachment) throws Exception;
	
}

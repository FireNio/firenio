package com.gifisan.nio.server.service.impl;

import com.gifisan.nio.component.SessionFactory;
import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.server.IOSession;
import com.gifisan.nio.server.service.NIOServlet;

public class PutSession2FactoryServlet extends NIOServlet{
	
	public static final String SERVICE_NAME = PutSession2FactoryServlet.class.getSimpleName();

	public void accept(IOSession session,ServerReadFuture future) throws Exception {
		
		SessionFactory factory = session.getContext().getSessionFactory();
		
		factory.putIOSession(session);
		
		future.write(session.getSessionID());
		
		session.flush(future);
	}
	
}

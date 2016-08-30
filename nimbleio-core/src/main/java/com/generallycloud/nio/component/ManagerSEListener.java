package com.generallycloud.nio.component;


public class ManagerSEListener extends SEListenerAdapter{
	
	public void sessionOpened(Session session) {

		NIOContext context = session.getContext();
		
		SessionFactory factory = context.getSessionFactory();

		factory.putSession(session);
	}

	public void sessionClosed(Session session) {
		
		NIOContext context = session.getContext();
		
		SessionFactory factory = context.getSessionFactory();

		factory.removeSession(session);
	}
	
}

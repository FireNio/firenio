package com.generallycloud.nio.component;


public class ManagerSEListener extends SEListenerAdapter{
	
	public void sessionOpened(Session session) {

		BaseContext context = session.getContext();
		
		SessionManager manager = context.getSessionManager();

		manager.putSession(session);
	}

	public void sessionClosed(Session session) {
		
		BaseContext context = session.getContext();
		
		SessionManager manager = context.getSessionManager();

		manager.removeSession(session);
	}
	
}

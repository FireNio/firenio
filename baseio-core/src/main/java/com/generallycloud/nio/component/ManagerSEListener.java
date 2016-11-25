package com.generallycloud.nio.component;


public class ManagerSEListener extends SEListenerAdapter{
	
	public void sessionOpened(SocketSession session) {

		SocketChannelContext context = session.getContext();
		
		SessionManager manager = context.getSessionManager();

		manager.putSession(session);
	}

	public void sessionClosed(SocketSession session) {
		
		SocketChannelContext context = session.getContext();
		
		SessionManager manager = context.getSessionManager();

		manager.removeSession(session);
	}
	
}

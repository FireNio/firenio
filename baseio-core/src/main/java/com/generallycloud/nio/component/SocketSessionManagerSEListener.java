package com.generallycloud.nio.component;


public class SocketSessionManagerSEListener extends SocketSEListenerAdapter{
	
	public void sessionOpened(SocketSession session) {

		SocketChannelContext context = session.getContext();
		
		SocketSessionManager manager = context.getSessionManager();

		manager.putSession(session);
	}

	public void sessionClosed(SocketSession session) {
		
		SocketChannelContext context = session.getContext();
		
		SocketSessionManager manager = context.getSessionManager();

		manager.removeSession(session);
	}
	
}

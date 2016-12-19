package com.generallycloud.nio.component;


public class SocketSessionManagerSEListener extends SocketSEListenerAdapter{
	
	@Override
	public void sessionOpened(SocketSession session) {

		SocketChannelContext context = session.getContext();
		
		SocketSessionManager manager = context.getSessionManager();

		manager.putSession(session);
	}

	@Override
	public void sessionClosed(SocketSession session) {
		
		SocketChannelContext context = session.getContext();
		
		SocketSessionManager manager = context.getSessionManager();

		manager.removeSession(session);
	}
	
}

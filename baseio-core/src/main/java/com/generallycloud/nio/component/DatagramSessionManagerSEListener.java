package com.generallycloud.nio.component;


public class DatagramSessionManagerSEListener extends DatagramSEListenerAdapter{
	
	public void sessionClosed(DatagramSession session) {
		
		DatagramChannelContext context = session.getContext();
		
		DatagramSessionManager manager = context.getSessionManager();

		manager.removeSession(session);
	}
	
}

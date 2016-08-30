package com.gifisan.nio.component;


public interface SessionEventListener {

	public abstract void sessionOpened(Session session);

	public abstract void sessionClosed(Session session);
	
	public abstract void sessionIdled(Session session,long lastIdleTime,long currentTime);
	
}

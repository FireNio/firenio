package com.gifisan.nio.component;


public interface SessionEventListener {

	public abstract void sessionOpened(Session session);

	public abstract void sessionClosed(Session session);
	
}

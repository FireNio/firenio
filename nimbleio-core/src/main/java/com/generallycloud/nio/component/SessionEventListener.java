package com.generallycloud.nio.component;

import java.util.EventListener;

public interface SessionEventListener extends EventListener{

	public abstract void sessionOpened(Session session);

	public abstract void sessionClosed(Session session);
	
	public abstract void sessionIdled(Session session,long lastIdleTime,long currentTime);
	
}

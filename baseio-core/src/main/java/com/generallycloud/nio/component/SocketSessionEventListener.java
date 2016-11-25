package com.generallycloud.nio.component;

import java.util.EventListener;

public interface SocketSessionEventListener extends EventListener{

	public abstract void sessionOpened(SocketSession session);

	public abstract void sessionClosed(SocketSession session);
	
	public abstract void sessionIdled(SocketSession session,long lastIdleTime,long currentTime);
	
}

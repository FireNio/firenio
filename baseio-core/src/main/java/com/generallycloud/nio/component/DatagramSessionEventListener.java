package com.generallycloud.nio.component;

import java.util.EventListener;

public interface DatagramSessionEventListener extends EventListener{

	public abstract void sessionOpened(DatagramSession session);

	public abstract void sessionClosed(DatagramSession session);
	
	public abstract void sessionIdled(DatagramSession session,long lastIdleTime,long currentTime);
	
}

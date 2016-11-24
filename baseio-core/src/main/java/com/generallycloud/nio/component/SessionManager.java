package com.generallycloud.nio.component;

import java.io.Closeable;

public interface SessionManager extends Closeable{

	public abstract void putSession(SocketSession session);

	public abstract SocketSession getSession(Integer sessionID);

	public abstract void removeSession(SocketSession session);

	public abstract void offerSessionMEvent(SessionMEvent event);

	public abstract int getManagedSessionSize();
	
	public abstract void loop();

}
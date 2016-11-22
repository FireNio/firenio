package com.generallycloud.nio.component;

import java.io.Closeable;

public interface SessionManager extends Closeable{

	public abstract void putSession(Session session);

	public abstract Session getSession(Integer sessionID);

	public abstract void removeSession(Session session);

	public abstract void offerSessionMEvent(SessionMEvent event);

	public abstract int getManagedSessionSize();
	
	public abstract void loop();

}
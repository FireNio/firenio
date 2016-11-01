package com.generallycloud.nio.component;

import com.generallycloud.nio.Looper;

public interface SessionManager extends Looper{

	public abstract void putSession(Session session);

	public abstract Session getSession(Integer sessionID);

	public abstract void removeSession(Session session);

	public abstract void offerSessionMEvent(SessionMEvent event);

	public abstract int getManagedSessionSize();

}
package com.generallycloud.nio.component;

import java.util.Map;

public interface SocketSessionManager extends SessionManager{
	
	public abstract void putSession(SocketSession session);

	public abstract void removeSession(SocketSession session);

	public abstract SocketSession getSession(Integer sessionID);

	public abstract void offerSessionMEvent(SocketSessionManagerEvent event);
	
	public interface SocketSessionManagerEvent {

		public abstract void fire(SocketChannelContext context, Map<Integer, SocketSession> sessions);
	}
}

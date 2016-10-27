package com.generallycloud.nio.balance.router;

import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.component.concurrent.ReentrantMap;

public abstract class AbstractFrontRouter implements FrontRouter{

	private ReentrantMap<Integer, SocketSession> clients = new ReentrantMap<Integer, SocketSession>();

	public void addClientSession(SocketSession session) {
		this.clients.put(session.getSessionID(), session);
	}

	public SocketSession getClientSession(Integer sessionID) {
		return clients.get(sessionID);
	}

	public void removeClientSession(SocketSession session) {
		this.clients.remove(session.getSessionID());
	}
}

package com.generallycloud.nio.balance.router;

import com.generallycloud.nio.component.IOSession;
import com.generallycloud.nio.component.concurrent.ReentrantMap;

public abstract class AbstractFrontRouter implements FrontRouter{

	private ReentrantMap<Integer, IOSession> clients = new ReentrantMap<Integer, IOSession>();

	public void addClientSession(IOSession session) {
		this.clients.put(session.getSessionID(), session);
	}

	public IOSession getClientSession(Integer sessionID) {
		return clients.get(sessionID);
	}

	public void removeClientSession(IOSession session) {
		this.clients.remove(session.getSessionID());
	}
}

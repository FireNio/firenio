package com.generallycloud.nio.front;

import com.generallycloud.nio.component.concurrent.ReentrantMap;

public class FrontRouter {

	private ReentrantMap<Integer, FrontFacadeSocketSession> clients = new ReentrantMap<>();

	public void addClientSession(FrontFacadeSocketSession session) {
		this.clients.put(session.getSessionID(), session);
	}

	public FrontFacadeSocketSession getClientSession(Integer sessionID) {
		return clients.get(sessionID);
	}

	public void removeClientSession(FrontFacadeSocketSession session) {
		this.clients.remove(session.getSessionID());
	}
	
}

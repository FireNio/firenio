package com.generallycloud.nio.balance.router;

import com.generallycloud.nio.balance.BalanceFacadeSocketSession;
import com.generallycloud.nio.component.concurrent.ReentrantMap;

public abstract class AbstractBalanceRouter implements BalanceRouter{

	private ReentrantMap<Integer, BalanceFacadeSocketSession> clients = new ReentrantMap<>();

	public void addClientSession(BalanceFacadeSocketSession session) {
		this.clients.put(session.getSessionID(), session);
	}

	public BalanceFacadeSocketSession getClientSession(Integer sessionID) {
		return clients.get(sessionID);
	}

	public void removeClientSession(BalanceFacadeSocketSession session) {
		this.clients.remove(session.getSessionID());
	}
}

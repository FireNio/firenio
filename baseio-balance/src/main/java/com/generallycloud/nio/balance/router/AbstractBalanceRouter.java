package com.generallycloud.nio.balance.router;

import com.generallycloud.nio.balance.BalanceFacadeSocketSession;
import com.generallycloud.nio.balance.BalanceReverseSocketSession;
import com.generallycloud.nio.component.concurrent.ReentrantMap;

public abstract class AbstractBalanceRouter implements BalanceRouter{

	private ReentrantMap<Integer, BalanceFacadeSocketSession> clients = new ReentrantMap<>();

	@Override
	public void addClientSession(BalanceFacadeSocketSession session) {
		this.clients.put(session.getSessionID(), session);
	}

	@Override
	public BalanceFacadeSocketSession getClientSession(Integer sessionID) {
		return clients.get(sessionID);
	}

	@Override
	public void removeClientSession(BalanceFacadeSocketSession session) {
		this.clients.remove(session.getSessionID());
	}
	
	@Override
	public BalanceReverseSocketSession getRouterSession(BalanceFacadeSocketSession session) {
		return session.getReverseSocketSession();
	}
}

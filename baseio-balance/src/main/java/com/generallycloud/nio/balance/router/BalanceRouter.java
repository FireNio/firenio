package com.generallycloud.nio.balance.router;

import com.generallycloud.nio.balance.BalanceFacadeSocketSession;
import com.generallycloud.nio.balance.BalanceReverseSocketSession;
import com.generallycloud.nio.protocol.ReadFuture;

public interface BalanceRouter {

	public abstract void addClientSession(BalanceFacadeSocketSession session);

	public abstract void addRouterSession(BalanceReverseSocketSession session);
	
	public abstract BalanceFacadeSocketSession getClientSession(Integer sessionID);

	public abstract BalanceReverseSocketSession getRouterSession(BalanceFacadeSocketSession session);
	
	public abstract BalanceReverseSocketSession getRouterSession(BalanceFacadeSocketSession session, ReadFuture future);

	public abstract void removeClientSession(BalanceFacadeSocketSession session);

	public abstract void removeRouterSession(BalanceReverseSocketSession session);

}
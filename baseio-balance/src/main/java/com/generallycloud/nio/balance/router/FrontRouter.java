package com.generallycloud.nio.balance.router;

import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.ReadFuture;

public interface FrontRouter {

	public abstract void addClientSession(SocketSession session);

	public abstract void addRouterSession(SocketSession session);
	
	public abstract SocketSession getClientSession(Integer sessionID);

	public abstract SocketSession getRouterSession(SocketSession session);
	
	public abstract SocketSession getRouterSession(SocketSession session, ReadFuture future);

	public abstract void removeClientSession(SocketSession session);

	public abstract void removeRouterSession(SocketSession session);

}
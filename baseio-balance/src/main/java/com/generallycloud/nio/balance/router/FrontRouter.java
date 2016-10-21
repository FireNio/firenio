package com.generallycloud.nio.balance.router;

import com.generallycloud.nio.component.IOSession;
import com.generallycloud.nio.protocol.ReadFuture;

public interface FrontRouter {

	public abstract void addClientSession(IOSession session);

	public abstract void addRouterSession(IOSession session);
	
	public abstract IOSession getClientSession(Integer sessionID);

	public abstract IOSession getRouterSession(IOSession session);
	
	public abstract IOSession getRouterSession(IOSession session, ReadFuture future);

	public abstract void removeClientSession(IOSession session);

	public abstract void removeRouterSession(IOSession session);

}
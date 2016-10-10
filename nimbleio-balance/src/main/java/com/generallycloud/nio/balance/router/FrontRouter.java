package com.generallycloud.nio.balance.router;

import com.generallycloud.nio.component.IOSession;
import com.generallycloud.nio.protocol.ReadFuture;

public interface FrontRouter {

	public abstract void addRouterSession(IOSession session);

	public abstract void removeRouterSession(IOSession session);

	public abstract IOSession getRouterSession(IOSession session, ReadFuture future);

	public abstract IOSession getRouterSession(IOSession session);

}
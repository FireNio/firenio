package com.gifisan.nio.server.selector;

import com.gifisan.nio.server.ServerEndPoint;
import com.gifisan.nio.server.ServiceAccept;

public interface ServiceAcceptor extends ServiceAccept, Runnable{

	public abstract void acceptException(Throwable exception);
	
	public abstract ServiceAcceptor update(ServerEndPoint endPoint);
}

package com.gifisan.nio.server.selector;

import com.gifisan.nio.component.ProtocolData;
import com.gifisan.nio.server.ServerEndPoint;
import com.gifisan.nio.server.ServiceAcceptor;

public interface ServiceAcceptorJob extends ServiceAcceptor, Runnable{

	public abstract void accept(Throwable exception);
	
	public abstract ServiceAcceptorJob update(ServerEndPoint endPoint,ProtocolData decoder);
	
	
}

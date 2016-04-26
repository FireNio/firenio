package com.gifisan.nio.client;

import com.gifisan.nio.component.ReadFuture;
import com.gifisan.nio.component.ClientStreamAcceptor;

public interface ProtectedClientSession extends ClientSession{
	
	public abstract void offer(ReadFuture future) ;

	public abstract void offer() ;
	
	public abstract ClientStreamAcceptor getStreamAcceptor(String serviceName);

}

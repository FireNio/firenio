package com.gifisan.nio.client;

import com.gifisan.nio.component.future.ReadFuture;

public interface ProtectedClientSession extends ClientSession{
	
	public abstract void offer(ReadFuture future) ;

	public abstract void offer() ;
	
	public abstract ClientStreamAcceptor getStreamAcceptor(String serviceName);

}

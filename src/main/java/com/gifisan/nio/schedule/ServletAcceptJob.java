package com.gifisan.nio.schedule;

import com.gifisan.nio.server.ServerEndPoint;
import com.gifisan.nio.server.ServletAcceptor;

public interface ServletAcceptJob extends ServletAcceptor, Job{

	public abstract void acceptException(Throwable exception);
	
	public abstract ServletAcceptJob update(ServerEndPoint endPoint);
}

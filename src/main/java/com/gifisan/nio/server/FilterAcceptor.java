package com.gifisan.nio.server;

import com.gifisan.nio.component.ReadFuture;
import com.gifisan.nio.server.session.IOSession;

public interface FilterAcceptor {
	
	public abstract void accept(IOSession session, ReadFuture future) throws Exception;

}

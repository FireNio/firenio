package com.gifisan.nio.server;

import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.server.session.IOSession;
public interface FilterAcceptor {
	
	public abstract void accept(IOSession session, ServerReadFuture future) throws Exception;

}

package com.gifisan.nio.component;

import com.gifisan.nio.server.session.IOSession;

public interface FilterAcceptor {
	
	public abstract void accept(IOSession session, ReadFuture future) throws Exception;

}

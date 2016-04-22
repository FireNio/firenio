package com.gifisan.nio.component;

import com.gifisan.nio.server.session.Session;

public interface ReadFutureAcceptor {

	public abstract void accept(Session session ,IOReadFuture future);
	
}

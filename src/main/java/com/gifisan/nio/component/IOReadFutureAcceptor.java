package com.gifisan.nio.component;

import com.gifisan.nio.component.protocol.IOReadFuture;

public interface IOReadFutureAcceptor {

	public abstract void accept(Session session ,IOReadFuture future) throws Exception;
	
}

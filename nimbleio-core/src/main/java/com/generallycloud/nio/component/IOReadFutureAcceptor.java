package com.generallycloud.nio.component;

import com.generallycloud.nio.protocol.IOReadFuture;

public interface IOReadFutureAcceptor {

	public abstract void accept(Session session ,IOReadFuture future) throws Exception;
	
}

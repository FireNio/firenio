package com.generallycloud.nio.component;

import com.generallycloud.nio.component.protocol.IOReadFuture;

public interface IOReadFutureAcceptor {

	public abstract void accept(Session session ,IOReadFuture future) throws Exception;
	
}

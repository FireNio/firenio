package com.generallycloud.nio.component;

import com.generallycloud.nio.protocol.ReadFuture;

public interface ReadFutureAcceptor {

	public abstract void accept(Session session ,ReadFuture future) throws Exception;
	
}

package com.generallycloud.nio.component;

import com.generallycloud.nio.protocol.ReadFuture;

public interface ReadFutureAcceptor {

	public abstract void accept(SocketSession session ,ReadFuture future) throws Exception;
	
}

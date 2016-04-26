package com.gifisan.nio.component;

import com.gifisan.nio.client.ClientSession;

public interface ClientStreamAcceptor {

	public abstract void accept(ClientSession session, ReadFuture future) throws Exception;
}

package com.gifisan.nio.client;

import com.gifisan.nio.component.future.ReadFuture;

public interface ClientStreamAcceptor {

	public abstract void accept(FixedSession session, ReadFuture future) throws Exception;
}

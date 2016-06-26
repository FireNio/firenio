package com.gifisan.nio.connector;

import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.extend.FixedSession;

public interface ClientStreamAcceptor {

	public abstract void accept(FixedSession session, ReadFuture future) throws Exception;
}

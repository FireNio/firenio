package com.gifisan.nio.extend;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.nio.future.NIOReadFuture;

public interface NIOReadFutureAcceptor {

	public abstract void accept(Session session ,NIOReadFuture future) throws Exception;
	
}

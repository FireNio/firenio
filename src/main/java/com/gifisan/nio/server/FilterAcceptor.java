package com.gifisan.nio.server;

import com.gifisan.nio.component.future.ReadFuture;
public interface FilterAcceptor {
	
	public abstract void accept(IOSession session, ReadFuture future) throws Exception;

}

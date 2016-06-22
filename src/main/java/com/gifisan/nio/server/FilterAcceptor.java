package com.gifisan.nio.server;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;
public interface FilterAcceptor {
	
	public abstract void accept(Session session, ReadFuture future) throws Exception;

}

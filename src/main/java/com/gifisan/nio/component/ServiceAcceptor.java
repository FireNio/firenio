package com.gifisan.nio.component;

import com.gifisan.nio.component.future.IOReadFuture;

public interface ServiceAcceptor {

	public abstract void accept(Session session, IOReadFuture future) throws Exception;

}

package com.gifisan.nio.component;

import com.gifisan.nio.component.future.ReadFuture;

public interface ReadFutureAcceptor {

	public abstract void accept(Session session ,ReadFuture future);
	
}

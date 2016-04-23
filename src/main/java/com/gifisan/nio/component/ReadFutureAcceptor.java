package com.gifisan.nio.component;


public interface ReadFutureAcceptor {

	public abstract void accept(Session session ,ReadFuture future);
	
}

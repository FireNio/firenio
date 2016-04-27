package com.gifisan.nio.component;

import com.gifisan.nio.component.future.IOReadFuture;


public interface ReadFutureAcceptor extends ServiceAcceptor{

	public abstract void accept(Session session ,IOReadFuture future);
	
}

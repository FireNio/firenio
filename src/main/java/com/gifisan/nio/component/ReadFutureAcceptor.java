package com.gifisan.nio.component;

import com.gifisan.nio.service.ServiceAcceptor;


public interface ReadFutureAcceptor extends ServiceAcceptor{

	public abstract void accept(Session session ,IOReadFuture future);
	
}

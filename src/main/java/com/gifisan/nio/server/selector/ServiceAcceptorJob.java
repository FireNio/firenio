package com.gifisan.nio.server.selector;

import com.gifisan.nio.component.ReadFuture;
import com.gifisan.nio.server.session.Session;
import com.gifisan.nio.service.ServiceAcceptor;

public interface ServiceAcceptorJob extends ServiceAcceptor, Runnable{

	public abstract void update(ReadFuture future);
	
	public abstract void accept(Session session, ReadFuture future) ;
	
}

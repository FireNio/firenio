package com.gifisan.nio.extend.service;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.future.ReadFuture;
import com.gifisan.nio.component.protocol.nio.future.NIOReadFuture;

public abstract class NIOFutureAcceptorService extends FutureAcceptorService{

	public void accept(Session session, ReadFuture future) throws Exception {
		this.doAccept(session, (NIOReadFuture) future);
	}

	protected abstract void doAccept(Session session, NIOReadFuture future) throws Exception;
	
}

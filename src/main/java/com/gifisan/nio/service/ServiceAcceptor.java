package com.gifisan.nio.service;

import com.gifisan.nio.component.ReadFuture;
import com.gifisan.nio.server.session.Session;

public interface ServiceAcceptor {

	public abstract void accept(Session session, ReadFuture future) throws Exception;

}

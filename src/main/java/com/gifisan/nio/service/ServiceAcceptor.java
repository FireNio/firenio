package com.gifisan.nio.service;

import com.gifisan.nio.component.IOReadFuture;
import com.gifisan.nio.component.Session;

public interface ServiceAcceptor {

	public abstract void accept(Session session, IOReadFuture future) throws Exception;

}

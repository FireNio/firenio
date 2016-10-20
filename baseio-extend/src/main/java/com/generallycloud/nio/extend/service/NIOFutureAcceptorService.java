package com.generallycloud.nio.extend.service;

import com.generallycloud.nio.codec.nio.future.NIOReadFuture;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.extend.implementation.ErrorServlet;
import com.generallycloud.nio.protocol.ReadFuture;

public abstract class NIOFutureAcceptorService extends FutureAcceptorService{
	
	private Logger logger = LoggerFactory.getLogger(NIOFutureAcceptorService.class);

	public void accept(Session session, ReadFuture future) throws Exception {
		this.doAccept(session, (NIOReadFuture) future);
	}

	protected abstract void doAccept(Session session, NIOReadFuture future) throws Exception;
	
	public void exceptionCaught(Session session, ReadFuture future, Exception cause, IOEventState state) {
		
		if (state == IOEventState.HANDLE) {
			
			ErrorServlet servlet = new ErrorServlet(cause);
			try {
				servlet.accept(session, future);
			} catch (Throwable e) {
				logger.error(e.getMessage(), e);
			}
			
		}
	}
}

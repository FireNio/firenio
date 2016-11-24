package com.generallycloud.nio.extend.service;

import com.generallycloud.nio.codec.base.future.BaseReadFuture;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.extend.implementation.ErrorServlet;
import com.generallycloud.nio.protocol.ReadFuture;

public abstract class BaseFutureAcceptorService extends FutureAcceptorService{
	
	private Logger logger = LoggerFactory.getLogger(BaseFutureAcceptorService.class);

	public void accept(SocketSession session, ReadFuture future) throws Exception {
		this.doAccept(session, (BaseReadFuture) future);
	}

	protected abstract void doAccept(SocketSession session, BaseReadFuture future) throws Exception;
	
	public void exceptionCaught(SocketSession session, ReadFuture future, Exception cause, IoEventState state) {
		
		if (state == IoEventState.HANDLE) {
			
			ErrorServlet servlet = new ErrorServlet(cause);
			try {
				servlet.accept(session, future);
			} catch (Throwable e) {
				logger.error(e.getMessage(), e);
			}
			
		}
	}
}

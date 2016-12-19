package com.generallycloud.nio.container.http11.service;

import com.generallycloud.nio.codec.http11.HttpContext;
import com.generallycloud.nio.codec.http11.HttpSession;
import com.generallycloud.nio.codec.http11.HttpSessionManager;
import com.generallycloud.nio.codec.http11.future.HttpReadFuture;
import com.generallycloud.nio.codec.http11.future.HttpStatus;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.container.service.FutureAcceptorService;
import com.generallycloud.nio.protocol.ReadFuture;

public abstract class HttpFutureAcceptorService extends FutureAcceptorService {
	
	private HttpContext		context	= HttpContext.getInstance();

	@Override
	public void accept(SocketSession session, ReadFuture future) throws Exception {

		HttpSessionManager manager = context.getHttpSessionManager();

		HttpReadFuture httpReadFuture = (HttpReadFuture) future;

		HttpSession httpSession = manager.getHttpSession(context,session, httpReadFuture);

		this.doAccept(httpSession, httpReadFuture);
	}

	protected abstract void doAccept(HttpSession session, HttpReadFuture future) throws Exception;

	@Override
	public void exceptionCaught(SocketSession session, ReadFuture future, Exception cause, IoEventState state) {
		
		if (state == IoEventState.HANDLE) {
			
			if (future instanceof HttpReadFuture) {
				((HttpReadFuture)future).setStatus(HttpStatus.C500);
			}
			
			future.write("server error:"+cause.getMessage());

			session.flush(future);
		}
	}
}

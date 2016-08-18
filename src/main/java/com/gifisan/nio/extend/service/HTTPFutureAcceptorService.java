package com.gifisan.nio.extend.service;

import java.io.IOException;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.future.ReadFuture;
import com.gifisan.nio.component.protocol.http11.future.HttpReadFuture;
import com.gifisan.nio.component.protocol.http11.future.HttpStatus;
import com.gifisan.nio.extend.plugin.http.HttpContext;
import com.gifisan.nio.extend.plugin.http.HttpSession;
import com.gifisan.nio.extend.plugin.http.HttpSessionFactory;

public abstract class HTTPFutureAcceptorService extends FutureAcceptorService {
	
	private Logger logger = LoggerFactory.getLogger(HTTPFutureAcceptorService.class);

	private HttpContext		context	= HttpContext.getInstance();

	public void accept(Session session, ReadFuture future) throws Exception {

		HttpSessionFactory factory = context.getHttpSessionFactory();

		HttpReadFuture httpReadFuture = (HttpReadFuture) future;

		HttpSession httpSession = factory.getHttpSession(context,session, httpReadFuture);

		this.doAccept(httpSession, httpReadFuture);
	}

	protected abstract void doAccept(HttpSession session, HttpReadFuture future) throws Exception;

	public void exceptionCaught(Session session, ReadFuture future, Exception cause, IOEventState state) {
		
		if (state == IOEventState.HANDLE) {
			
			if (future instanceof HttpReadFuture) {
				((HttpReadFuture)future).setStatus(HttpStatus.C500);
			}
			
			future.write("server error:"+cause.getMessage());
			
			try {
				session.flush(future);
			} catch (IOException e) {
				logger.error(e.getMessage(),e);
			}
		}
		
	}
}

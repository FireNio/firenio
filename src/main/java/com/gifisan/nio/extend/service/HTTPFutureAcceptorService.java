package com.gifisan.nio.extend.service;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.future.ReadFuture;
import com.gifisan.nio.component.protocol.http11.future.HttpReadFuture;
import com.gifisan.nio.component.protocol.http11.future.WebSocketReadFuture;
import com.gifisan.nio.extend.plugin.http.HttpContext;
import com.gifisan.nio.extend.plugin.http.HttpSession;
import com.gifisan.nio.extend.plugin.http.HttpSessionFactory;

public abstract class HTTPFutureAcceptorService extends FutureAcceptorService {

	private HttpContext		context	= HttpContext.getInstance();

	public void accept(Session session, ReadFuture future) throws Exception {

		HttpSessionFactory factory = context.getHttpSessionFactory();

		HttpReadFuture httpReadFuture = (HttpReadFuture) future;

		HttpSession httpSession = factory.getHttpSession(context,session, httpReadFuture);

		this.doAccept(httpSession, httpReadFuture);
	}

	protected abstract void doAccept(HttpSession session, HttpReadFuture future) throws Exception;

	public void exceptionCaughtOnRead(Session session, ReadFuture future, Exception cause) {
		
		if (future instanceof HttpReadFuture) {
			
			HttpReadFuture f = (HttpReadFuture)future;
			
			f.setStatus(500);
			
			f.write("server error:"+cause.getMessage());
			
			session.flush(f);
			
		}else if(future instanceof WebSocketReadFuture){
			
			WebSocketReadFuture f = (WebSocketReadFuture)future;
			
			f.write("server error:"+cause.getMessage());
			
			session.flush(f);
		}
	}
}

package com.gifisan.nio.extend.service;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.future.ReadFuture;
import com.gifisan.nio.component.protocol.http11.future.HTTPReadFuture;
import com.gifisan.nio.extend.http11.HttpSession;
import com.gifisan.nio.extend.http11.HttpSessionFactory;
import com.gifisan.nio.extend.plugin.http.HttpContext;

public abstract class HTTPFutureAcceptorService extends FutureAcceptorService {

	private HttpContext		context	= HttpContext.getInstance();

	public void accept(Session session, ReadFuture future) throws Exception {

		HttpSessionFactory factory = context.getHttpSessionFactory();

		HTTPReadFuture httpReadFuture = (HTTPReadFuture) future;

		HttpSession httpSession = factory.getHttpSession(context,session, httpReadFuture);

		this.doAccept(httpSession, httpReadFuture);
	}

	protected abstract void doAccept(HttpSession session, HTTPReadFuture future) throws Exception;

}

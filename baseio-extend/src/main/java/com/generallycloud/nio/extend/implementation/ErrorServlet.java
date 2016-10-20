package com.generallycloud.nio.extend.implementation;

import com.generallycloud.nio.codec.base.future.BaseReadFuture;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.extend.RESMessage;
import com.generallycloud.nio.extend.service.BaseFutureAcceptorService;

public class ErrorServlet extends BaseFutureAcceptorService{
	
	public ErrorServlet(Throwable exception) {
		this.exception = exception;
	}

	protected void doAccept(Session session, BaseReadFuture future) throws Exception {
//		String stack = DebugUtil.exception2string(exception);
//		RESMessage message = new RESMessage(500, stack);
		RESMessage res = new RESMessage(500, exception.getMessage());
		future.write(res.toString());
		session.flush(future);
	}

	private Throwable exception = null;

	public Throwable getException() {
		return exception;
	}

	public void setException(Throwable exception) {
		this.exception = exception;
	}

}

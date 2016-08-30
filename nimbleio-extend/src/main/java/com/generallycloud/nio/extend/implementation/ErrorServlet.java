package com.generallycloud.nio.extend.implementation;

import com.generallycloud.nio.Encoding;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.protocol.nio.future.NIOReadFuture;
import com.generallycloud.nio.extend.RESMessage;
import com.generallycloud.nio.extend.service.NIOFutureAcceptorService;

public class ErrorServlet extends NIOFutureAcceptorService{
	
	public ErrorServlet(Throwable exception) {
		this.exception = exception;
	}

	protected void doAccept(Session session, NIOReadFuture future) throws Exception {
//		String stack = DebugUtil.exception2string(exception);
//		RESMessage message = new RESMessage(500, stack);
		RESMessage res = new RESMessage(500, exception.getMessage());
		future.write(res.toString(),Encoding.DEFAULT);
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

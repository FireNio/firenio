package com.gifisan.nio.extend.implementation;

import com.gifisan.nio.Encoding;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.nio.future.NIOReadFuture;
import com.gifisan.nio.extend.RESMessage;
import com.gifisan.nio.extend.service.FutureAcceptorService;

public class ErrorServlet extends FutureAcceptorService{
	
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

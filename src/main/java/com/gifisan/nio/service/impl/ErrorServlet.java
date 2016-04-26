package com.gifisan.nio.service.impl;

import com.gifisan.nio.Encoding;
import com.gifisan.nio.component.ReadFuture;
import com.gifisan.nio.server.RESMessage;
import com.gifisan.nio.server.session.IOSession;
import com.gifisan.nio.service.NIOServlet;

public class ErrorServlet extends NIOServlet{
	
	public ErrorServlet(Throwable exception) {
		this.exception = exception;
	}

	public void accept(IOSession session,ReadFuture future) throws Exception {
//		String stack = DebugUtil.exception2string(exception);
//		RESMessage message = new RESMessage(500, stack);
		RESMessage res = new RESMessage(500, exception.getMessage());
		session.write(res.toString(),Encoding.DEFAULT);
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

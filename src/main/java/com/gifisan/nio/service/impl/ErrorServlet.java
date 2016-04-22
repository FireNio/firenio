package com.gifisan.nio.service.impl;

import com.gifisan.nio.Encoding;
import com.gifisan.nio.component.Message;
import com.gifisan.nio.component.RESMessage;
import com.gifisan.nio.server.session.Session;
import com.gifisan.nio.service.NIOServlet;

public class ErrorServlet extends NIOServlet{
	
	public ErrorServlet(Throwable exception) {
		this.exception = exception;
	}

	public void accept(Session session,Message message) throws Exception {
//		String stack = DebugUtil.exception2string(exception);
//		RESMessage message = new RESMessage(500, stack);
		RESMessage res = new RESMessage(500, exception.getMessage());
		session.write(res.toString(),Encoding.DEFAULT);
		session.flush(message,null);
	}

	private Throwable exception = null;

	public Throwable getException() {
		return exception;
	}

	public void setException(Throwable exception) {
		this.exception = exception;
	}

}

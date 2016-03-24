package com.gifisan.nio.servlet.impl;

import com.gifisan.nio.Encoding;
import com.gifisan.nio.component.RESMessage;
import com.gifisan.nio.server.NIOServlet;
import com.gifisan.nio.server.Request;
import com.gifisan.nio.server.Response;

public class ErrorServlet extends NIOServlet{
	
	public ErrorServlet(Exception exception) {
		this.exception = exception;
	}

	public void accept(Request request, Response response) throws Exception {
//		String stack = DebugUtil.exception2string(exception);
//		RESMessage message = new RESMessage(500, stack);
		RESMessage message = new RESMessage(500, exception.getMessage());
		response.write(message.toString(),Encoding.DEFAULT);
		response.flush();
	}

	private Exception exception = null;

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}
	
}

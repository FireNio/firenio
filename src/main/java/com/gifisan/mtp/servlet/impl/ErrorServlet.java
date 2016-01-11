package com.gifisan.mtp.servlet.impl;

import com.gifisan.mtp.Encoding;
import com.gifisan.mtp.component.RESMessage;
import com.gifisan.mtp.server.MTPServlet;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;

public class ErrorServlet extends MTPServlet{
	
	public ErrorServlet(Exception exception) {
		this.exception = exception;
	}

	public void accept(Request request, Response response) throws Exception {
//		String stack = DebugUtil.exception2string(exception);
//		RESMessage message = new RESMessage(500, stack);
		RESMessage message = new RESMessage(500, exception.getMessage());
		response.write(message.toString().getBytes(Encoding.DEFAULT));
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

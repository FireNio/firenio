package com.gifisan.mtp.servlet.impl;

import com.gifisan.mtp.common.DebugUtil;
import com.gifisan.mtp.server.MTPServlet;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;

public class ErrorServlet extends MTPServlet{
	
	public ErrorServlet(Exception exception) {
		this.exception = exception;
	}

	public void accept(Request request, Response response) throws Exception {
		String stack = DebugUtil.exception2string(exception);
//		JSONObject object = new JSONObject();
//		object.put("msg", exception.getMessage());
//		object.put("stack", stack);
//		String message = object.toJSONString();
		response.write(stack);
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

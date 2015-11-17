package com.yoocent.mtp.servlet.impl;

import com.yoocent.mtp.common.DebugUtil;
import com.yoocent.mtp.server.MTPServlet;
import com.yoocent.mtp.server.Request;
import com.yoocent.mtp.server.Response;

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
		response.setErrorResponse();
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

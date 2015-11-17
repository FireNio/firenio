package com.yoocent.mtp.servlet.test;

import com.yoocent.mtp.common.StringUtil;
import com.yoocent.mtp.server.MTPServlet;
import com.yoocent.mtp.server.Request;
import com.yoocent.mtp.server.Response;

public class TestSimpleServlet extends MTPServlet{

	public static String SERVICE_KEY = TestSimpleServlet.class.getSimpleName();
	
	public void accept(Request request, Response response) throws Exception {
		String test = request.getStringParameter("test");
		if (StringUtil.isBlankOrNull(test)) {
			test = "test";
		}
		
		response.write("$".getBytes());
		response.write(test.getBytes());
		response.write("$".getBytes());
		response.flush();
		
	}

}

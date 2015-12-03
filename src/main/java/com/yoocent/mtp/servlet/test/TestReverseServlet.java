package com.yoocent.mtp.servlet.test;

import com.yoocent.mtp.common.StringUtil;
import com.yoocent.mtp.server.MTPServlet;
import com.yoocent.mtp.server.Request;
import com.yoocent.mtp.server.Response;

public class TestReverseServlet extends MTPServlet{

	public static String SERVICE_NAME = TestReverseServlet.class.getSimpleName();
	
	public void accept(Request request, Response response) throws Exception {

		String test = request.getStringParameter("param");

		if (StringUtil.isBlankOrNull(test)) {
			test = "test";
		}
		
		for (int i = 0; i < 10; i++) {
			response.write("$".getBytes());
			response.write(test.getBytes());

			response.write(request.getSession().getSessionID().getBytes());

			response.write("$".getBytes());
			response.flush();
			Thread.sleep(1000);
			
		}
		
		
	}

}

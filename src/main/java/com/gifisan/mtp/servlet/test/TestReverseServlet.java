package com.gifisan.mtp.servlet.test;

import com.gifisan.mtp.common.StringUtil;
import com.gifisan.mtp.server.MTPServlet;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;

public class TestReverseServlet extends MTPServlet{

	public static String SERVICE_NAME = TestReverseServlet.class.getSimpleName();
	
	public void accept(Request request, Response response) throws Exception {

		String test = request.getParameter("param");

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

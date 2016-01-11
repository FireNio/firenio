package com.gifisan.mtp.servlet.test;

import com.gifisan.mtp.common.StringUtil;
import com.gifisan.mtp.server.MTPServlet;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;

public class TestSimpleServlet extends MTPServlet{

	public static String SERVICE_NAME = TestSimpleServlet.class.getSimpleName();
	
	public void accept(Request request, Response response) throws Exception {

		String test = request.getContent();

		if (StringUtil.isNullOrBlank(test)) {
			test = "test";
		}
		
		response.write("$");
		response.write(test);

		response.write(request.getSession().getSessionID());

		response.write("$");
		response.flush();
		
	}

}

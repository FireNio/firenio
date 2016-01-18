package com.gifisan.mtp.servlet.test;

import com.gifisan.mtp.common.StringUtil;
import com.gifisan.mtp.server.MTPServlet;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;

public class TestSimpleServlet extends MTPServlet{

	public void accept(Request request, Response response) throws Exception {

		String test = request.getContent();

		if (StringUtil.isNullOrBlank(test)) {
			test = "test";
		}
		
		response.write("$");
		response.write(test);

		response.write("$");
		response.flush();
		
	}

}

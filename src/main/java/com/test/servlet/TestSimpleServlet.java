package com.test.servlet;

import com.gifisan.mtp.common.StringUtil;
import com.gifisan.mtp.server.MTPServlet;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;

public class TestSimpleServlet extends MTPServlet{
	
	private TestSimple1 simple1 = new TestSimple1();

	public void accept(Request request, Response response) throws Exception {

		String test = request.getContent();

		if (StringUtil.isNullOrBlank(test)) {
			test = "test";
		}
		response.write(simple1.dynamic());
		response.write(test);
		response.write("$");
		response.flush();
		
	}

}

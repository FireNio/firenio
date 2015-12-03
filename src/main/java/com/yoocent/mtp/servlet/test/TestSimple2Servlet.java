package com.yoocent.mtp.servlet.test;

import com.yoocent.mtp.common.StringUtil;
import com.yoocent.mtp.component.ServletConfig;
import com.yoocent.mtp.server.MTPServlet;
import com.yoocent.mtp.server.Request;
import com.yoocent.mtp.server.Response;
import com.yoocent.mtp.server.context.ServletContext;

public class TestSimple2Servlet extends MTPServlet{
	
	public static final String SERVICE_NAME = TestSimple2Servlet.class.getSimpleName();
	
	public void accept(Request request, Response response) throws Exception {
		String test = request.getStringParameter("test");
		if (StringUtil.isBlankOrNull(test)) {
			test = "test222";
		}
		
		response.write("@".getBytes());
		response.write(test.getBytes());
		response.flush();
		
	}

	public void initialize(ServletContext context, ServletConfig config)
			throws Exception {
		throw new Exception("因为某些原因没有加载成功！");
	}
	
	

}

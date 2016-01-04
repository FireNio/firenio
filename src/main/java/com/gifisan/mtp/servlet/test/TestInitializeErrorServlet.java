package com.gifisan.mtp.servlet.test;

import com.gifisan.mtp.common.StringUtil;
import com.gifisan.mtp.component.ServletConfig;
import com.gifisan.mtp.server.MTPServlet;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;
import com.gifisan.mtp.server.ServletContext;

public class TestInitializeErrorServlet extends MTPServlet{
	
	public static final String SERVICE_NAME = TestInitializeErrorServlet.class.getSimpleName();
	
	public void accept(Request request, Response response) throws Exception {
		String test = request.getParameter("test");
		if (StringUtil.isNullOrBlank(test)) {
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

package com.test.servlet;

import com.gifisan.mtp.component.Configuration;
import com.gifisan.mtp.server.MTPServlet;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;
import com.gifisan.mtp.server.ServerContext;

public class TestInitializeErrorServlet extends MTPServlet{
	
	public void accept(Request request, Response response) throws Exception {
		
	}

	public void initialize(ServerContext context, Configuration config)
			throws Exception {
		throw new Exception("因为某些原因没有加载成功！");
	}

}

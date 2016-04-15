package com.test.servlet;

import com.gifisan.nio.component.Configuration;
import com.gifisan.nio.server.ServerContext;
import com.gifisan.nio.service.NIOServlet;
import com.gifisan.nio.service.Request;
import com.gifisan.nio.service.Response;

public class TestInitializeErrorServlet extends NIOServlet{
	
	public void accept(Request request, Response response) throws Exception {
		
	}

	public void initialize(ServerContext context, Configuration config)
			throws Exception {
		throw new Exception("因为某些原因没有加载成功！");
	}

}

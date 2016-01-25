package com.gifisan.mtp.servlet.impl.test;

import com.gifisan.mtp.component.ServletConfig;
import com.gifisan.mtp.server.MTPServlet;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;
import com.gifisan.mtp.server.ServerContext;

public class TestInitializeErrorServlet extends MTPServlet{
	
	public void accept(Request request, Response response) throws Exception {
		
	}

	public void initialize(ServerContext context, ServletConfig config)
			throws Exception {
		throw new Exception("因为某些原因没有加载成功！");
	}
	
	

}

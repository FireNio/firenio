package com.test.servlet;

import java.io.IOException;

import com.gifisan.nio.server.NIOServlet;
import com.gifisan.nio.server.Request;
import com.gifisan.nio.server.Response;

public class TestExceptionServlet extends NIOServlet{

	public void accept(Request request, Response response) throws Exception {
		throw new IOException("测试啊");
	}
	
}

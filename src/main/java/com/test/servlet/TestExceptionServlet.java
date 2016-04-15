package com.test.servlet;

import java.io.IOException;

import com.gifisan.nio.service.NIOServlet;
import com.gifisan.nio.service.Request;
import com.gifisan.nio.service.Response;

public class TestExceptionServlet extends NIOServlet{

	public void accept(Request request, Response response) throws Exception {
		throw new IOException("测试啊");
	}
	
}

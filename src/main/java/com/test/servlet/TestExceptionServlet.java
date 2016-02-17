package com.test.servlet;

import java.io.IOException;

import com.gifisan.mtp.server.MTPServlet;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;

public class TestExceptionServlet extends MTPServlet{

	public void accept(Request request, Response response) throws Exception {
		throw new IOException("测试啊");
	}
	
}

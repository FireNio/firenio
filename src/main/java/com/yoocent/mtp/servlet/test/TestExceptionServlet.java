package com.yoocent.mtp.servlet.test;

import java.io.IOException;

import com.yoocent.mtp.server.MTPServlet;
import com.yoocent.mtp.server.Request;
import com.yoocent.mtp.server.Response;

public class TestExceptionServlet extends MTPServlet{

	public static final String SERVICE_NAME = TestExceptionServlet.class.getSimpleName();
	
	public void accept(Request request, Response response) throws Exception {
		throw new IOException("测试啊");
	}
	
}

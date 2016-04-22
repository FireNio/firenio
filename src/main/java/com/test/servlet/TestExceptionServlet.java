package com.test.servlet;

import java.io.IOException;

import com.gifisan.nio.server.session.Session;
import com.gifisan.nio.service.NIOServlet;

public class TestExceptionServlet extends NIOServlet{

	public void accept(Session session) throws Exception {
		throw new IOException("测试啊");
	}
	
}

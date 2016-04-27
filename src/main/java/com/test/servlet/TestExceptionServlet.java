package com.test.servlet;

import java.io.IOException;

import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.server.session.IOSession;
import com.gifisan.nio.service.NIOServlet;

public class TestExceptionServlet extends NIOServlet{

	public void accept(IOSession session,ServerReadFuture future) throws Exception {
		throw new IOException("测试啊");
	}
	
}

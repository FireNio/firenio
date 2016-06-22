package com.test.servlet;

import java.io.IOException;

import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.server.IOSession;
import com.gifisan.nio.server.service.NIOServlet;

public class TestExceptionServlet extends NIOServlet{
	
	public static final String SERVICE_NAME = TestExceptionServlet.class.getSimpleName();

	public void accept(IOSession session,ReadFuture future) throws Exception {
		throw new IOException("测试啊");
	}
	
}

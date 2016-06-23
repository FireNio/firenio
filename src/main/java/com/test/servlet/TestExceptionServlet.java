package com.test.servlet;

import java.io.IOException;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.server.service.NIOFutureAcceptor;

public class TestExceptionServlet extends NIOFutureAcceptor{
	
	public static final String SERVICE_NAME = TestExceptionServlet.class.getSimpleName();

	public void accept(Session session,ReadFuture future) throws Exception {
		throw new IOException("测试啊");
	}
	
}

package com.test.servlet;

import java.io.IOException;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.nio.NIOReadFuture;
import com.gifisan.nio.extend.service.FutureAcceptorService;

public class TestExceptionServlet extends FutureAcceptorService{
	
	public static final String SERVICE_NAME = TestExceptionServlet.class.getSimpleName();

	protected void doAccept(Session session, NIOReadFuture future) throws Exception {
		throw new IOException("测试啊");
	}
	
}

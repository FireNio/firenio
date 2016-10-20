package com.test.service.nio;

import java.io.IOException;

import com.generallycloud.nio.codec.nio.future.NIOReadFuture;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.extend.service.NIOFutureAcceptorService;

public class TestExceptionServlet extends NIOFutureAcceptorService{
	
	public static final String SERVICE_NAME = TestExceptionServlet.class.getSimpleName();

	protected void doAccept(Session session, NIOReadFuture future) throws Exception {
		throw new IOException("测试啊");
	}
	
}

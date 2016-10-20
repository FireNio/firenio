package com.test.service.nio;

import java.io.IOException;

import com.generallycloud.nio.codec.base.future.BaseReadFuture;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.extend.service.BaseFutureAcceptorService;

public class TestExceptionServlet extends BaseFutureAcceptorService{
	
	public static final String SERVICE_NAME = TestExceptionServlet.class.getSimpleName();

	protected void doAccept(Session session, BaseReadFuture future) throws Exception {
		throw new IOException("测试啊");
	}
	
}

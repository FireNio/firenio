package com.generallycloud.nio.container.protobase.example;

import java.io.IOException;

import com.generallycloud.nio.codec.base.future.BaseReadFuture;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.container.protobase.service.BaseFutureAcceptorService;

public class TestExceptionServlet extends BaseFutureAcceptorService{
	
	public static final String SERVICE_NAME = TestExceptionServlet.class.getSimpleName();

	protected void doAccept(SocketSession session, BaseReadFuture future) throws Exception {
		throw new IOException("测试啊");
	}
	
}

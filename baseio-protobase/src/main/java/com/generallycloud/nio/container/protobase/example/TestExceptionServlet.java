package com.generallycloud.nio.container.protobase.example;

import java.io.IOException;

import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFuture;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.container.protobase.service.ProtobaseFutureAcceptorService;

public class TestExceptionServlet extends ProtobaseFutureAcceptorService{
	
	public static final String SERVICE_NAME = TestExceptionServlet.class.getSimpleName();

	@Override
	protected void doAccept(SocketSession session, ProtobaseReadFuture future) throws Exception {
		throw new IOException("测试啊");
	}
	
}

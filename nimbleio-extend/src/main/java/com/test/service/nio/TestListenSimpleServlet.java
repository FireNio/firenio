package com.test.service.nio;

import com.generallycloud.nio.codec.nio.future.NIOReadFuture;
import com.generallycloud.nio.codec.nio.future.NIOReadFutureImpl;
import com.generallycloud.nio.common.StringUtil;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.extend.service.NIOFutureAcceptorService;

public class TestListenSimpleServlet extends NIOFutureAcceptorService{
	
	public static final String SERVICE_NAME = TestListenSimpleServlet.class.getSimpleName();
	
	protected void doAccept(Session session, NIOReadFuture future) throws Exception {

		String test = future.getText();

		if (StringUtil.isNullOrBlank(test)) {
			test = "test";
		}
		
		future.write(test);
		future.write("$");
		session.flush(future);
		
		for (int i = 0; i < 5; i++) {
			
			NIOReadFuture f = new NIOReadFutureImpl(future.getFutureID(),future.getFutureName());
			
			f.write(test);
			f.write("$");
			
			session.flush(f);
		}
		
	}

}

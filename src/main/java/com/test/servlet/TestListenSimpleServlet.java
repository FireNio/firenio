package com.test.servlet;

import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.component.ReadFutureFactory;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.nio.NIOReadFuture;
import com.gifisan.nio.extend.service.FutureAcceptorService;

public class TestListenSimpleServlet extends FutureAcceptorService{
	
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
			
			future = ReadFutureFactory.create(session,future);
			
			future.write(test);
			future.write("$");
			
			session.flush(future);
		}
		
	}

}

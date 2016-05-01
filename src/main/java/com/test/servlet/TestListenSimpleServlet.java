package com.test.servlet;

import com.gifisan.nio.common.ReadFutureFactory;
import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.server.IOSession;
import com.gifisan.nio.server.service.NIOServlet;

public class TestListenSimpleServlet extends NIOServlet{
	
	
	public void accept(IOSession session,ServerReadFuture future) throws Exception {

		String test = future.getText();

		if (StringUtil.isNullOrBlank(test)) {
			test = "test";
		}
		
		future.write(test);
		future.write("$");
		session.flush(future);
		
		for (int i = 0; i < 5; i++) {
			
			future = ReadFutureFactory.create(future);
			
			future.write(test);
			future.write("$");
			
			session.flush(future);
		}
		
	}

}

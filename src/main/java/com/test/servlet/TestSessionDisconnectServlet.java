package com.test.servlet;

import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.server.IOSession;
import com.gifisan.nio.server.service.NIOServlet;

public class TestSessionDisconnectServlet extends NIOServlet{
	
	public static final String SERVICE_NAME = TestSessionDisconnectServlet.class.getSimpleName();
	
	private TestSimple1 simple1 = new TestSimple1();
	
//	private AtomicInteger size = new AtomicInteger();

	public void accept(IOSession session,ReadFuture future) throws Exception {

		String test = future.getText();

		if (StringUtil.isNullOrBlank(test)) {
			test = "test";
		}
		future.write(simple1.dynamic());
		future.write(test);
		future.write("$");
		session.flush(future);
		
		session.disconnect();
		
	}

}

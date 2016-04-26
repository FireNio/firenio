package com.test.servlet;

import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.component.ReadFuture;
import com.gifisan.nio.server.session.IOSession;
import com.gifisan.nio.service.NIOServlet;

public class TestSimpleServlet extends NIOServlet{
	
	private TestSimple1 simple1 = new TestSimple1();
	
//	private AtomicInteger size = new AtomicInteger();

	public void accept(IOSession session,ReadFuture future) throws Exception {

		String test = future.getText();

		if (StringUtil.isNullOrBlank(test)) {
			test = "test";
		}
		session.write(simple1.dynamic());
		session.write(test);
		session.write("$");
		session.flush(future);
		
//		System.out.println("=============================="+size.incrementAndGet());
	}

}

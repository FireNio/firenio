package com.test.servlet;

import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.server.session.Session;
import com.gifisan.nio.service.NIOServlet;

public class TestSimpleServlet extends NIOServlet{
	
	private TestSimple1 simple1 = new TestSimple1();
	
//	private AtomicInteger size = new AtomicInteger();

	public void accept(Session session) throws Exception {

		String test = session.getRequestText();

		if (StringUtil.isNullOrBlank(test)) {
			test = "test";
		}
		session.write(simple1.dynamic());
		session.write(test);
		session.write("$");
		session.flush();
		
//		System.out.println("=============================="+size.incrementAndGet());
	}

}

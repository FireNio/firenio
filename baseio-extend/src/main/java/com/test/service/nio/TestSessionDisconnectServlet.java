package com.test.service.nio;

import com.generallycloud.nio.codec.base.future.BaseReadFuture;
import com.generallycloud.nio.common.StringUtil;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.extend.service.BaseFutureAcceptorService;

public class TestSessionDisconnectServlet extends BaseFutureAcceptorService{
	
	public static final String SERVICE_NAME = TestSessionDisconnectServlet.class.getSimpleName();
	
	private TestSimple1 simple1 = new TestSimple1();
	
//	private AtomicInteger size = new AtomicInteger();

	protected void doAccept(Session session, BaseReadFuture future) throws Exception {

		String test = future.getText();

		if (StringUtil.isNullOrBlank(test)) {
			test = "test";
		}
		future.write(simple1.dynamic());
		future.write(test);
		future.write("$");
		session.flush(future);
		
		session.close();
		
	}

}

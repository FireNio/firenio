package com.test.service.nio;

import com.generallycloud.nio.codec.base.future.BaseReadFuture;
import com.generallycloud.nio.codec.base.future.BaseReadFutureImpl;
import com.generallycloud.nio.common.StringUtil;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.extend.service.BaseFutureAcceptorService;

public class TestListenSimpleServlet extends BaseFutureAcceptorService{
	
	public static final String SERVICE_NAME = TestListenSimpleServlet.class.getSimpleName();
	
	protected void doAccept(Session session, BaseReadFuture future) throws Exception {

		String test = future.getText();

		if (StringUtil.isNullOrBlank(test)) {
			test = "test";
		}
		
		future.write(test);
		future.write("$");
		session.flush(future);
		
		for (int i = 0; i < 5; i++) {
			
			BaseReadFuture f = new BaseReadFutureImpl(session.getContext(),future.getFutureID(),future.getFutureName());
			
			f.write(test);
			f.write("$");
			
			session.flush(f);
		}
		
	}

}

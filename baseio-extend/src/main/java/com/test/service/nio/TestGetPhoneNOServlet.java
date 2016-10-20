package com.test.service.nio;

import com.generallycloud.nio.codec.base.future.BaseReadFuture;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.extend.service.NIOFutureAcceptorService;

public class TestGetPhoneNOServlet extends NIOFutureAcceptorService {
	
	public static final String SERVICE_NAME = TestGetPhoneNOServlet.class.getSimpleName();

	private String [] NOS = {"13811112222","18599991111","18599991111","13811112222"};
	
	private int index = 0;

	protected void doAccept(Session session, BaseReadFuture future) throws Exception {

		String phone = NOS[index++];
		
		if (index == 4) {
			index = 0;
		}
		
		future.write(phone);
		
		session.flush(future);
	}

}

package com.test.servlet;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.nio.NIOReadFuture;
import com.gifisan.nio.extend.service.FutureAcceptorService;

public class TestGetPhoneNOServlet extends FutureAcceptorService {
	
	public static final String SERVICE_NAME = TestGetPhoneNOServlet.class.getSimpleName();

	private String [] NOS = {"13811112222","18599991111","18599991111","13811112222"};
	
	private int index = 0;

	protected void doAccept(Session session, NIOReadFuture future) throws Exception {

		String phone = NOS[index++];
		
		if (index == 4) {
			index = 0;
		}
		
		future.write(phone);
		
		session.flush(future);
	}

}

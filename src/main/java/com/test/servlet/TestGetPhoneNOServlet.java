package com.test.servlet;

import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.server.service.NIOServlet;
import com.gifisan.nio.server.session.IOSession;

public class TestGetPhoneNOServlet extends NIOServlet {

	private String [] NOS = {"13811112222","18599991111","18599991111","13811112222"};
	
	private int index = 0;

	public void accept(IOSession session,ServerReadFuture future) throws Exception {

		String phone = NOS[index++];
		
		if (index == 4) {
			index = 0;
		}
		
		future.write(phone);
		
		session.flush(future);
	}

}

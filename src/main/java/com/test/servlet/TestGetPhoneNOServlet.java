package com.test.servlet;

import com.gifisan.nio.server.session.Session;
import com.gifisan.nio.service.NIOServlet;

public class TestGetPhoneNOServlet extends NIOServlet {

	private String [] NOS = {"13811112222","18599991111","18599991111","13811112222"};
	
	private int index = 0;

	public void accept(Session session) throws Exception {

		String phone = NOS[index++];
		
		if (index == 4) {
			index = 0;
		}
		
		session.write(phone);
		
		session.flush();
	}

}

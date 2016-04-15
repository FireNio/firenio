package com.test.servlet;

import com.gifisan.nio.service.NIOServlet;
import com.gifisan.nio.service.Request;
import com.gifisan.nio.service.Response;

public class TestGetPhoneNOServlet extends NIOServlet {

	private String [] NOS = {"13811112222","18599991111","18599991111","13811112222"};
	
	private int index = 0;

	public void accept(Request request, Response response) throws Exception {

		String phone = NOS[index++];
		
		if (index == 4) {
			index = 0;
		}
		
		response.write(phone);
		
		response.flush();
	}

}

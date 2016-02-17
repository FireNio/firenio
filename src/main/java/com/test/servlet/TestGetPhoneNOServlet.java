package com.test.servlet;

import com.gifisan.mtp.server.MTPServlet;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;

public class TestGetPhoneNOServlet extends MTPServlet {

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

package com.test.service.http;

import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.common.UUIDGenerator;
import com.gifisan.nio.component.protocol.http11.future.Cookie;
import com.gifisan.nio.component.protocol.http11.future.HttpReadFuture;
import com.gifisan.nio.extend.plugin.http.HttpSession;
import com.gifisan.nio.extend.service.HTTPFutureAcceptorService;

public class TestCookieHeaderServlet extends HTTPFutureAcceptorService {
	
	protected void doAccept(HttpSession session, HttpReadFuture future) throws Exception {
		
		System.out.println();

		System.out.println();
		
		String name = future.getRequestParam("name");
		String value = future.getRequestParam("value");
		
		if (StringUtil.isNullOrBlank(name)) {
			name = "test8";
		}
		
		if (StringUtil.isNullOrBlank(value)) {
			value = UUIDGenerator.random();
		}
		
		String res = "yes server already accept your message :) " + future.getRequestParams();

		Cookie c = new Cookie(name, value);
		
		c.setComment("comment");
		c.setMaxAge(999999);
		
		future.addCookie(c);
		future.setHeader(name, value);
		
		future.write(res);
		
		session.flush(future);
	}
}

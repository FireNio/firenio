package com.test.servlet.http;

import com.gifisan.nio.component.protocol.http11.future.HttpReadFuture;
import com.gifisan.nio.extend.plugin.http.HttpSession;
import com.gifisan.nio.extend.service.HTTPFutureAcceptorService;

public class TestSimpleServlet extends HTTPFutureAcceptorService {
	
//	private Logger	logger	= LoggerFactory.getLogger(TestSimpleServlet.class);

	protected void doAccept(HttpSession session, HttpReadFuture future) throws Exception {
//		System.out.println();
//		logger.info(future.getHost());
//		logger.info(future.getRequestURI());
//		System.out.println();
		String res = "yes server already accept your message :) " + future.getRequestParams();

		future.write(res);
		
		session.flush(future);
	}
}

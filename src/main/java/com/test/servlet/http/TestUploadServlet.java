package com.test.servlet.http;

import com.gifisan.nio.component.BufferedOutputStream;
import com.gifisan.nio.component.protocol.http11.future.HTTPReadFuture;
import com.gifisan.nio.extend.http11.HttpSession;
import com.gifisan.nio.extend.service.HTTPFutureAcceptorService;

public class TestUploadServlet extends HTTPFutureAcceptorService {

	protected void doAccept(HttpSession session, HTTPReadFuture future) throws Exception {
		
		String res;

		if (future.hasOutputStream()) {

			if (future.getOutputStream() == null) {
				future.setOutputStream(new BufferedOutputStream());
				return;
			}

			BufferedOutputStream outputStream = (BufferedOutputStream) future.getOutputStream();

			res = "yes server already accept your message :) </BR><PRE style='font-size: 18px;color: #FF9800;'>" + outputStream.toString()+"</PRE>";
		} else {
			res = "yes server already accept your message :) " + future.getRequestURI();
		}

		future.write(res);
		session.flush(future);
	}

}

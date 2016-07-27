package com.test.servlet.http;

import com.gifisan.nio.component.BufferedOutputStream;
import com.gifisan.nio.component.protocol.http11.future.HttpReadFuture;
import com.gifisan.nio.extend.plugin.http.HttpSession;
import com.gifisan.nio.extend.service.HTTPFutureAcceptorService;

public class TestUploadServlet extends HTTPFutureAcceptorService {

	protected void doAccept(HttpSession session, HttpReadFuture future) throws Exception {
		
		String res;

		if (future.hasOutputStream()) {

			if (future.getOutputStream() == null) {
				future.setOutputStream(new BufferedOutputStream());
				return;
			}

			BufferedOutputStream outputStream = (BufferedOutputStream) future.getOutputStream();

			res = "yes server already accept your message :) "+future.getRequestParams()+" </BR><PRE style='font-size: 18px;color: #FF9800;'>" + outputStream.toString()+"</PRE>";
		} else {
			res = "yes server already accept your message :) " + future.getRequestParams();
		}

		future.write(res);
		session.flush(future);
	}

}

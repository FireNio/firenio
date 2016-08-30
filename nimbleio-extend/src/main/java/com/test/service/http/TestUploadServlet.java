package com.test.service.http;

import java.io.IOException;

import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.component.protocol.http11.HttpSession;
import com.generallycloud.nio.component.protocol.http11.future.HttpReadFuture;
import com.generallycloud.nio.extend.service.HTTPFutureAcceptorService;

public class TestUploadServlet extends HTTPFutureAcceptorService {

	protected void doAccept(HttpSession session, HttpReadFuture future) throws Exception {
		
		String res;

		if (future.hasOutputStream()) {

			if (future.getOutputStream() == null) {
				
				if (future.getContentLength() > 1024 * 1024) {
					throw new IOException("too large file");
				}
				
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

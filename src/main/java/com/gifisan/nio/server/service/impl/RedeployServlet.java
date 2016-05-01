package com.gifisan.nio.server.service.impl;

import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.server.IOSession;
import com.gifisan.nio.server.RESMessage;
import com.gifisan.nio.server.ServerContext;
import com.gifisan.nio.server.service.NIOServlet;

public class RedeployServlet extends NIOServlet {

	public void accept(IOSession session,ServerReadFuture future) throws Exception {
		if (session.getLoginCenter().validate(session, future)) {
			ServerContext context = (ServerContext) session.getContext();
			RESMessage message =  context.redeploy()  ? RESMessage.R_SUCCESS : RESMessage.R_FAIL;
			future.write(message.toString());
		} else {
			future.write(RESMessage.R_UNAUTH.toString());
		}
		session.flush(future);

	}

}

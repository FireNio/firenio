package com.gifisan.nio.server.service.impl;

import com.gifisan.nio.component.ApplicationContext;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.server.RESMessage;
import com.gifisan.nio.server.service.NIOServlet;

public class SYSTEMRedeployServlet extends NIOServlet {
	
	public static final String SERVICE_NAME = SYSTEMRedeployServlet.class.getSimpleName();

	public void accept(Session session,ReadFuture future) throws Exception {
		
		ApplicationContext context = ApplicationContext.getInstance();
		
		if (context.getLoginCenter().isValidate(session, future)) {
			RESMessage message =  context.redeploy()  ? RESMessage.SUCCESS : RESMessage.SYSTEM_ERROR;
			future.write(message.toString());
		} else {
			future.write(RESMessage.UNAUTH.toString());
		}
		session.flush(future);
	}

}

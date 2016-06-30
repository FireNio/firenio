package com.gifisan.nio.extend.implementation;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.extend.ApplicationContext;
import com.gifisan.nio.extend.RESMessage;
import com.gifisan.nio.extend.service.FutureAcceptorService;

public class SYSTEMRedeployServlet extends FutureAcceptorService {
	
	public static final String SERVICE_NAME = SYSTEMRedeployServlet.class.getSimpleName();

	public void accept(Session session,ReadFuture future) throws Exception {
		
		ApplicationContext context = ApplicationContext.getInstance();
		
		if (context.getLoginCenter().isValidate(future.getParameters())) {
			RESMessage message =  context.redeploy()  ? RESMessage.SUCCESS : RESMessage.SYSTEM_ERROR;
			future.write(message.toString());
		} else {
			future.write(RESMessage.UNAUTH.toString());
		}
		session.flush(future);
	}

}

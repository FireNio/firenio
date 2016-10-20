package com.generallycloud.nio.extend.implementation;

import com.generallycloud.nio.codec.base.future.BaseReadFuture;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.extend.ApplicationContext;
import com.generallycloud.nio.extend.RESMessage;
import com.generallycloud.nio.extend.service.BaseFutureAcceptorService;

public class SYSTEMRedeployServlet extends BaseFutureAcceptorService {
	
	public static final String SERVICE_NAME = SYSTEMRedeployServlet.class.getSimpleName();

	protected void doAccept(Session session, BaseReadFuture future) throws Exception {
		
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

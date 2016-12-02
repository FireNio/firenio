package com.generallycloud.nio.container.implementation;

import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.container.ApplicationContext;
import com.generallycloud.nio.container.RESMessage;
import com.generallycloud.nio.container.service.FutureAcceptorService;
import com.generallycloud.nio.protocol.ReadFuture;

public class SYSTEMRedeployServlet extends FutureAcceptorService {
	
	public static final String SERVICE_NAME = SYSTEMRedeployServlet.class.getSimpleName();

	public void accept(SocketSession session, ReadFuture future) throws Exception {
		
		ApplicationContext context = ApplicationContext.getInstance();
		
		RESMessage message =  context.redeploy()  ? RESMessage.SUCCESS : RESMessage.SYSTEM_ERROR;
		future.write(message.toString());
		
		session.flush(future);
	}

}

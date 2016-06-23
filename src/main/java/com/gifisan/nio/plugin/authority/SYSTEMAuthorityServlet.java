package com.gifisan.nio.plugin.authority;

import com.gifisan.nio.component.ApplicationContext;
import com.gifisan.nio.component.ApplicationContextUtil;
import com.gifisan.nio.component.LoginCenter;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.server.RESMessage;
import com.gifisan.nio.server.service.NIOFutureAcceptor;
import com.gifisan.security.Authority;

public class SYSTEMAuthorityServlet extends NIOFutureAcceptor{
	
	public static final String SERVICE_NAME = SYSTEMAuthorityServlet.class.getSimpleName();

	public void accept(Session session,ReadFuture future) throws Exception {
		
		LoginCenter loginCenter = ApplicationContext.getInstance().getLoginCenter();
		
		RESMessage message = RESMessage.UNAUTH;
		
		if (loginCenter.login(session, future)) {
			
			Authority authority = ApplicationContextUtil.getAuthority(session);
			
			message = new RESMessage(0, authority,null);
		}
		
		future.write(message.toString());
		
		session.flush(future);
	}
	
}

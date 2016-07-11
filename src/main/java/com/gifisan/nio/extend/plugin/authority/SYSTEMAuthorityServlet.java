package com.gifisan.nio.extend.plugin.authority;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.nio.NIOReadFuture;
import com.gifisan.nio.extend.ApplicationContext;
import com.gifisan.nio.extend.ApplicationContextUtil;
import com.gifisan.nio.extend.LoginCenter;
import com.gifisan.nio.extend.RESMessage;
import com.gifisan.nio.extend.security.Authority;
import com.gifisan.nio.extend.service.FutureAcceptorService;

public class SYSTEMAuthorityServlet extends FutureAcceptorService{
	
	public static final String SERVICE_NAME = SYSTEMAuthorityServlet.class.getSimpleName();

	protected void doAccept(Session session, NIOReadFuture future) throws Exception {
		
		LoginCenter loginCenter = ApplicationContext.getInstance().getLoginCenter();
		
		RESMessage message = RESMessage.UNAUTH;
		
		if (loginCenter.login(session, future.getParameters())) {
			
			Authority authority = ApplicationContextUtil.getAuthority(session);
			
			message = new RESMessage(0, authority,null);
		}
		
		future.write(message.toString());
		
		session.flush(future);
	}
	
}

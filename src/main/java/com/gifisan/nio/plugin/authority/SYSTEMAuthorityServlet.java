package com.gifisan.nio.plugin.authority;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.extend.ApplicationContext;
import com.gifisan.nio.extend.ApplicationContextUtil;
import com.gifisan.nio.extend.LoginCenter;
import com.gifisan.nio.extend.RESMessage;
import com.gifisan.nio.extend.service.FutureAcceptorService;
import com.gifisan.nio.security.Authority;

public class SYSTEMAuthorityServlet extends FutureAcceptorService{
	
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

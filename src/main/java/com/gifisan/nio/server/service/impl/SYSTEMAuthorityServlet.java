package com.gifisan.nio.server.service.impl;

import com.gifisan.nio.component.LoginCenter;
import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.server.IOSession;
import com.gifisan.nio.server.RESMessage;
import com.gifisan.nio.server.service.NIOServlet;
import com.gifisan.security.Authority;

public class SYSTEMAuthorityServlet extends NIOServlet{
	
	public static final String SERVICE_NAME = SYSTEMAuthorityServlet.class.getSimpleName();

	public void accept(IOSession session,ServerReadFuture future) throws Exception {
		
		LoginCenter loginCenter = session.getLoginCenter();
		
		RESMessage message = RESMessage.R_UNAUTH;
		
		if (loginCenter.login(session, future)) {
			
			Authority authority = session.getAuthority();
			
			message = new RESMessage(0, authority.getUUID()+";"+session.getSessionID());
		}
		
		future.write(message.toString());
		
		session.flush(future);
	}
	
}

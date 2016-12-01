package com.generallycloud.nio.container.authority;

import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.container.ApplicationContext;
import com.generallycloud.nio.container.ApplicationContextUtil;
import com.generallycloud.nio.container.LoginCenter;
import com.generallycloud.nio.container.service.FutureAcceptorService;
import com.generallycloud.nio.protocol.ReadFuture;

public abstract class SYSTEMAuthorityServlet extends FutureAcceptorService{
	
	public static final String SERVICE_NAME = SYSTEMAuthorityServlet.class.getSimpleName();

	public void accept(SocketSession session, ReadFuture future) throws Exception {
		
		LoginCenter loginCenter = ApplicationContext.getInstance().getLoginCenter();
		
		loginCenter.login(session, getUsername(future),getPassword(future));
			
		Authority authority = ApplicationContextUtil.getAuthority(session);
			
		writeRusult(future, authority);
		
		session.flush(future);
	}
	
	protected abstract String getUsername(ReadFuture future);	
	
	protected abstract String getPassword(ReadFuture future);
	
	protected abstract void writeRusult(ReadFuture future,Authority authority);
}

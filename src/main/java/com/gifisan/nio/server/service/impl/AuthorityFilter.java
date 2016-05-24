package com.gifisan.nio.server.service.impl;


import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.server.IOSession;
import com.gifisan.nio.server.ServerSession;
import com.gifisan.nio.server.service.AbstractNIOFilter;
import com.gifisan.security.AuthorityManager;

public class AuthorityFilter extends AbstractNIOFilter {

	private Logger		logger	= LoggerFactory.getLogger(AuthorityFilter.class);
	
	public void accept(IOSession session,ServerReadFuture future) throws Exception {
		
		AuthorityManager authorityManager = ((ServerSession)session).getAuthorityManager();
		
		if (authorityManager == null) {
			
			authorityManager = session.getContext().getRoleManager().getAuthorityManager(-1);
		}
		
		if (!authorityManager.isInvokeApproved(future.getServiceName())) {
			
			future.write("forbidden");
			
			session.flush(future);
			
			logger.debug("请求IP：{}，服务名称：{}，请求内容：{}", new String[] { 
						session.getRemoteAddr(), 
						future.getServiceName(),
						future.getText() });
		}
	}

}

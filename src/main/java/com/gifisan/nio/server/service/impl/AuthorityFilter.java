package com.gifisan.nio.server.service.impl;


import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.server.IOSession;
import com.gifisan.nio.server.ServerSession;
import com.gifisan.nio.server.service.AbstractNIOFilter;
import com.gifisan.security.Authority;
import com.gifisan.security.AuthorityManager;

public class AuthorityFilter extends AbstractNIOFilter {

	private Logger		logger	= LoggerFactory.getLogger(AuthorityFilter.class);
	
	public void accept(IOSession session,ReadFuture future) throws Exception {
		
		ServerSession _session =  ((ServerSession)session);
		
		AuthorityManager authorityManager = _session.getAuthorityManager();
		
		if (authorityManager == null) {
			
			authorityManager = session.getContext().getRoleManager().getAuthorityManager(Authority.GUEST);
			
			_session.setAuthorityManager(authorityManager);
		}
		
		if (!authorityManager.isInvokeApproved(future.getServiceName())) {
			
			future.write("forbidden");
			
			session.flush(future);
			
			logger.debug("已拒绝非法请求，请求IP：{}，服务名称：{}，请求内容：{}", new String[] { 
						session.getRemoteAddr(), 
						future.getServiceName(),
						future.getText() });
		}
	}

}

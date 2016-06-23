package com.gifisan.nio.server.service.impl;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.ApplicationContext;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.plugin.authority.AuthorityAttachment;
import com.gifisan.nio.plugin.authority.AuthorityPlugin;
import com.gifisan.nio.server.service.AbstractFutureAcceptor;
import com.gifisan.security.Authority;
import com.gifisan.security.AuthorityManager;

public class AuthorityFilter extends AbstractFutureAcceptor {

	private Logger		logger	= LoggerFactory.getLogger(AuthorityFilter.class);
	
	public void accept(Session session,ReadFuture future) throws Exception {
		
		AuthorityPlugin authorityPlugin = AuthorityPlugin.getInstance();
		
		AuthorityAttachment attachment = (AuthorityAttachment) session.getAttachment(authorityPlugin);
		
		AuthorityManager authorityManager = attachment.getAuthorityManager();
		
		if (authorityManager == null) {
			
			ApplicationContext context = ApplicationContext.getInstance();
			
			authorityManager = context.getRoleManager().getAuthorityManager(Authority.GUEST);
			
			attachment.setAuthorityManager(authorityManager);
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

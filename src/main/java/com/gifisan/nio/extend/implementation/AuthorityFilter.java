package com.gifisan.nio.extend.implementation;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.extend.ApplicationContext;
import com.gifisan.nio.extend.service.FutureAcceptorFilter;
import com.gifisan.nio.plugin.authority.AuthoritySessionAttachment;
import com.gifisan.nio.plugin.authority.AuthorityContext;
import com.gifisan.nio.security.Authority;
import com.gifisan.nio.security.AuthorityManager;

public class AuthorityFilter extends FutureAcceptorFilter {

	private Logger		logger	= LoggerFactory.getLogger(AuthorityFilter.class);
	
	public void accept(Session session,ReadFuture future) throws Exception {
		
		AuthorityContext pluginContext = AuthorityContext.getInstance();
		
		AuthoritySessionAttachment attachment = pluginContext.getSessionAttachment(session);
		
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

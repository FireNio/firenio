package com.generallycloud.nio.container.implementation;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.container.ApplicationContext;
import com.generallycloud.nio.container.authority.Authority;
import com.generallycloud.nio.container.authority.AuthorityContext;
import com.generallycloud.nio.container.authority.AuthorityManager;
import com.generallycloud.nio.container.authority.AuthoritySessionAttachment;
import com.generallycloud.nio.container.service.FutureAcceptorFilter;
import com.generallycloud.nio.protocol.NamedReadFuture;

public class AuthorityFilter extends FutureAcceptorFilter {

	private Logger logger = LoggerFactory.getLogger(AuthorityFilter.class);

	protected void accept(SocketSession session, NamedReadFuture future) throws Exception {

		AuthorityContext pluginContext = AuthorityContext.getInstance();

		AuthoritySessionAttachment attachment = pluginContext.getSessionAttachment(session);

		AuthorityManager authorityManager = attachment.getAuthorityManager();

		if (authorityManager == null) {

			ApplicationContext context = ApplicationContext.getInstance();

			authorityManager = context.getRoleManager().getAuthorityManager(Authority.GUEST);

			attachment.setAuthorityManager(authorityManager);
		}

		if (!authorityManager.isInvokeApproved(future.getFutureName())) {

			future.write("forbidden");

			session.flush(future);

			logger.debug("已拒绝非法请求，请求IP：{}，服务名称：{}，请求内容：{}",
					new String[] { session.getRemoteAddr(), future.getFutureName() });
		}
	}

}

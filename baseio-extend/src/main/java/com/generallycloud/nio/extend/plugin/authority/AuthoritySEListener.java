package com.generallycloud.nio.extend.plugin.authority;

import com.generallycloud.nio.component.SEListenerAdapter;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.extend.ApplicationContext;
import com.generallycloud.nio.extend.ApplicationContextUtil;
import com.generallycloud.nio.extend.FixedSessionFactory;
import com.generallycloud.nio.extend.security.Authority;

public class AuthoritySEListener extends SEListenerAdapter {
	
	public void sessionOpened(Session session) {
		
		AuthorityContext context = AuthorityContext.getInstance();
		
		AuthoritySessionAttachment attachment = context.getSessionAttachment(session);

		if (attachment == null) {

			attachment = new AuthoritySessionAttachment();

			session.setAttachment(context.getPluginIndex(), attachment);
		}

	}

	public void sessionClosed(Session session) {
		
		Authority authority = ApplicationContextUtil.getAuthority(session);
		
		if (authority == null) {
			return;
		}
		
		ApplicationContext context = ApplicationContext.getInstance();
		
		FixedSessionFactory sessionFactory = context.getSessionFactory();
		
		sessionFactory.removeSession(authority.getUsername());
	}
	
}

package com.gifisan.nio.extend.plugin.authority;

import com.gifisan.nio.component.SEListenerAdapter;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.extend.ApplicationContext;
import com.gifisan.nio.extend.ApplicationContextUtil;
import com.gifisan.nio.extend.FixedSessionFactory;
import com.gifisan.nio.extend.security.Authority;

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

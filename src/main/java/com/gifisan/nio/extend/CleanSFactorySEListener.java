package com.gifisan.nio.extend;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.SessionEventListener;
import com.gifisan.nio.extend.security.Authority;

public class CleanSFactorySEListener implements SessionEventListener{

	public void sessionOpened(Session session) {
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

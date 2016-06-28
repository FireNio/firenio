package com.gifisan.nio.extend.plugin.authority;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.SessionEventListener;

public class AuthoritySEListener implements SessionEventListener{
	
	public void sessionOpened(Session session) {
		
		AuthorityContext context = AuthorityContext.getInstance();
		
		AuthoritySessionAttachment attachment = context.getSessionAttachment(session);

		if (attachment == null) {

			attachment = new AuthoritySessionAttachment();

			session.setAttachment(context, attachment);
		}

	}

	public void sessionClosed(Session session) {
		
		AuthorityContext context = AuthorityContext.getInstance();
		
		
	}
	
}

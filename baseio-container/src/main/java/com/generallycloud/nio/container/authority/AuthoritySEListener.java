package com.generallycloud.nio.container.authority;

import com.generallycloud.nio.component.SocketSEListenerAdapter;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.container.ApplicationContextUtil;

public class AuthoritySEListener extends SocketSEListenerAdapter {
	
	public void sessionOpened(SocketSession session) {
		
		AuthorityContext context = AuthorityContext.getInstance();
		
		AuthoritySessionAttachment attachment = context.getSessionAttachment(session);

		if (attachment == null) {

			attachment = new AuthoritySessionAttachment();

			session.setAttachment(context.getPluginIndex(), attachment);
		}

	}

	public void sessionClosed(SocketSession session) {
		
		Authority authority = ApplicationContextUtil.getAuthority(session);
		
		if (authority == null) {
			return;
		}
		
//		AuthorityContext context = AuthorityContext.getInstance();
//		
//		session.setAttachment(context.getPluginIndex(), null);
	}
	
}

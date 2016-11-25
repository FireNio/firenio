package com.generallycloud.nio.extend.plugin.authority;

import com.generallycloud.nio.component.SocketSEListenerAdapter;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.extend.ApplicationContextUtil;
import com.generallycloud.nio.extend.security.Authority;

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

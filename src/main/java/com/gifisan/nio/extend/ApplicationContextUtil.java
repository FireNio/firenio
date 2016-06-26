package com.gifisan.nio.extend;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.plugin.authority.AuthoritySessionAttachment;
import com.gifisan.nio.plugin.authority.AuthorityContext;
import com.gifisan.nio.security.Authority;
import com.gifisan.nio.security.AuthorityManager;

public class ApplicationContextUtil {

	public static AuthorityManager getAuthorityManager(Session session){
		
		AuthorityContext plugin = AuthorityContext.getInstance();
		
		AuthoritySessionAttachment attachment = (AuthoritySessionAttachment) session.getAttachment(plugin);
		
		return attachment.getAuthorityManager();
	}
	
	public static Authority getAuthority(Session session){
		
		AuthorityManager authorityManager = getAuthorityManager(session);
		
		return authorityManager.getAuthority();
	}
	
	public static Authority getAuthority(FixedSession session){
		
		return getAuthority(session.getSession());
	}
	
}

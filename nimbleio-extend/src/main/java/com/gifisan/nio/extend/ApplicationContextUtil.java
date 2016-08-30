package com.gifisan.nio.extend;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.extend.plugin.authority.AuthorityContext;
import com.gifisan.nio.extend.plugin.authority.AuthoritySessionAttachment;
import com.gifisan.nio.extend.security.Authority;
import com.gifisan.nio.extend.security.AuthorityManager;

public class ApplicationContextUtil {

	public static AuthorityManager getAuthorityManager(Session session){
		
		AuthorityContext plugin = AuthorityContext.getInstance();
		
		AuthoritySessionAttachment attachment = (AuthoritySessionAttachment) session.getAttachment(plugin.getPluginIndex());
		
		if (attachment == null) {
			return null;
		}
		
		return attachment.getAuthorityManager();
	}
	
	public static Authority getAuthority(Session session){
		
		AuthorityManager authorityManager = getAuthorityManager(session);
		
		if (authorityManager == null) {
			return null;
		}
		
		return authorityManager.getAuthority();
	}
	
}

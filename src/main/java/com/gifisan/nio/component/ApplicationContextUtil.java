package com.gifisan.nio.component;

import com.gifisan.nio.plugin.authority.AuthorityAttachment;
import com.gifisan.nio.plugin.authority.AuthorityPlugin;
import com.gifisan.security.Authority;
import com.gifisan.security.AuthorityManager;

public class ApplicationContextUtil {

	public static AuthorityManager getAuthorityManager(Session session){
		
		AuthorityPlugin plugin = AuthorityPlugin.getInstance();
		
		AuthorityAttachment attachment = (AuthorityAttachment) session.getAttachment(plugin);
		
		return attachment.getAuthorityManager();
	}
	
	public static Authority getAuthority(Session session){
		
		AuthorityManager authorityManager = getAuthorityManager(session);
		
		return authorityManager.getAuthority();
	}
	
}

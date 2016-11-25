package com.generallycloud.nio.extend;

import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.extend.plugin.authority.AuthorityContext;
import com.generallycloud.nio.extend.plugin.authority.AuthoritySessionAttachment;
import com.generallycloud.nio.extend.security.Authority;
import com.generallycloud.nio.extend.security.AuthorityManager;

public class ApplicationContextUtil {

	public static AuthorityManager getAuthorityManager(SocketSession session){
		
		AuthorityContext plugin = AuthorityContext.getInstance();
		
		AuthoritySessionAttachment attachment = (AuthoritySessionAttachment) session.getAttachment(plugin.getPluginIndex());
		
		if (attachment == null) {
			return null;
		}
		
		return attachment.getAuthorityManager();
	}
	
	public static Authority getAuthority(SocketSession session){
		
		AuthorityManager authorityManager = getAuthorityManager(session);
		
		if (authorityManager == null) {
			return null;
		}
		
		return authorityManager.getAuthority();
	}
	
}

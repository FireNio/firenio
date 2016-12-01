package com.generallycloud.nio.container;

import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.container.authority.Authority;
import com.generallycloud.nio.container.authority.AuthorityContext;
import com.generallycloud.nio.container.authority.AuthorityManager;
import com.generallycloud.nio.container.authority.AuthoritySessionAttachment;

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

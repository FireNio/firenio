package com.generallycloud.nio.extend.plugin.authority;

import com.generallycloud.nio.extend.security.Authority;
import com.generallycloud.nio.extend.security.AuthorityManager;

public class AuthoritySessionAttachment {

	private AuthorityManager authorityManager = null;

	public AuthorityManager getAuthorityManager() {
		return authorityManager;
	}
	
	public void setAuthorityManager(AuthorityManager authorityManager) {
		this.authorityManager = authorityManager;
		if (authorityManager.getAuthority().getRoleID() == Authority.GUEST.getRoleID()) {
			return;
		}
	}
	

}

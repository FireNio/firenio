package com.gifisan.nio.plugin.authority;

import com.gifisan.nio.common.UUIDGenerator;
import com.gifisan.security.Authority;
import com.gifisan.security.AuthorityManager;

public class Test {
	

	private AuthorityManager		authorityManager	= null;
	
	public Authority getAuthority() {

		return authorityManager.getAuthority();
	}
	
	public AuthorityManager getAuthorityManager() {
		return authorityManager;
	}

	public void setAuthorityManager(AuthorityManager authorityManager) {
		this.authorityManager = authorityManager;
		if (authorityManager.getAuthority().getRoleID() == Authority.GUEST.getRoleID()) {
			return;
		}
		this.sessionID = UUIDGenerator.random();
		this.context.getSessionFactory().putIOSession(this);
	}

}

package com.gifisan.nio.plugin.authority;

import com.gifisan.nio.Attachment;
import com.gifisan.security.AuthorityManager;

public class AuthorityAttachment implements Attachment {

	private AuthorityManager authorityManager = null;

	public AuthorityManager getAuthorityManager() {
		return authorityManager;
	}

	public void setAuthorityManager(AuthorityManager authorityManager) {
		this.authorityManager = authorityManager;
	}
	
	
	

}

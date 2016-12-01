package com.generallycloud.nio.container.authority;

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

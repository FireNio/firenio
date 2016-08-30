package com.likemessage.bean;

import com.generallycloud.nio.extend.security.Authority;

public class T_USER extends Authority {

	private String		nickname;
	private String		phoneNo;
	private boolean	deleted;

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getPhoneNo() {
		return phoneNo;
	}

	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

}

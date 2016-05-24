package com.gifisan.security;

public class Authority {

	private String username;
	
	private Integer userID;
		
	private Integer roleID;
	
	private String password;

	protected void setPassword(String password) {
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	protected void setUsername(String username) {
		this.username = username;
	}

	public Integer getUserID() {
		return userID;
	}

	protected void setUserID(Integer userID) {
		this.userID = userID;
	}

	public Integer getRoleID() {
		return roleID;
	}

	protected String getPassword() {
		return password;
	}

	protected void setRoleID(Integer roleID) {
		this.roleID = roleID;
	}
	
}

package com.gifisan.security;

public class Authority {
	
	public static final Authority GUEST = new Authority(){{
		setRoleID(-1);
	}};

	private String password;
	
	private Integer roleID;

	private Integer userID;
		
	private String username;
	
	public Authority() {
		
	}

	public Authority(String username, String UUID) {
		this.username = username;
		this.UUID = UUID;
	}

	private String UUID;
	
	protected String getPassword() {
		return password;
	}

	public Integer getRoleID() {
		return roleID;
	}

	public Integer getUserID() {
		return userID;
	}

	public String getUsername() {
		return username;
	}

	public String getUUID() {
		return UUID;
	}

	protected void setPassword(String password) {
		this.password = password;
	}

	protected void setRoleID(Integer roleID) {
		this.roleID = roleID;
	}

	protected void setUserID(Integer userID) {
		this.userID = userID;
	}

	protected void setUsername(String username) {
		this.username = username;
	}

	protected void setUUID(String UUID) {
		this.UUID = UUID;
	}
	
}

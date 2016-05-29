package com.gifisan.security;

public class Authority {
	
	public static final Authority GUEST = new Authority(){{
		setRoleID(-1);
	}};

	private String className;
	
	private String password;

	private Integer roleID;
		
	private String sessionID;
	
	private Integer userID;
	
	private String username;
	
	private String UUID;

	public Authority() {
		this.className = this.getClass().getName();
	}

	public Authority(String username, String UUID) {
		this.username = username;
		this.UUID = UUID;
	}

	public String getClassName() {
		return className;
	}

	public String getPassword() {
		return password;
	}

	public Integer getRoleID() {
		return roleID;
	}

	public String getSessionID() {
		return sessionID;
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

	public void setClassName(String className) {
		this.className = className;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setRoleID(Integer roleID) {
		this.roleID = roleID;
	}

	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}

	public void setUserID(Integer userID) {
		this.userID = userID;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setUUID(String UUID) {
		this.UUID = UUID;
	}
	
}

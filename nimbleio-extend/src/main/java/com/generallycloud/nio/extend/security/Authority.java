package com.generallycloud.nio.extend.security;

public class Authority {
	
	public static final Authority GUEST = new Authority(){{
		setRoleID(-1);
	}};

	private String className;
	
	private String password;

	private Integer roleID;
		
	private Integer userID;
	
	private String username;
	
	private String uuid;

	public Authority() {
		this.className = this.getClass().getName();
	}

	public Authority(String username, String uuid) {
		this.username = username;
		this.uuid = uuid;
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
	
	public Integer getUserID() {
		return userID;
	}

	public String getUsername() {
		return username;
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

	public void setUserID(Integer userID) {
		this.userID = userID;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	public String toString() {
		return "authority:"+username;
	}
}

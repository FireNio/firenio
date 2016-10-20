package com.generallycloud.nio.extend.security;

import java.util.List;

import com.generallycloud.nio.common.MessageFormatter;

public class Role{

	private Integer		roleID;
	private String			roleName;
	private String			description;
	private List<Integer>	children;
	private List<Integer>	permissions;

	public Integer getRoleID() {
		return roleID;
	}

	public String getRoleName() {
		return roleName;
	}

	public List<Integer> getChildren() {
		return children;
	}

	public List<Integer> getPermissions() {
		return permissions;
	}

	protected void setRoleID(Integer roleID) {
		this.roleID = roleID;
	}

	protected void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	protected void setChildren(List<Integer> children) {
		this.children = children;
	}

	protected void setPermissions(List<Integer> permissions) {
		this.permissions = permissions;
	}

	public String getDescription() {
		return description;
	}

	protected void setDescription(String description) {
		this.description = description;
	}
	
	public String toString() {
		return MessageFormatter.format("[id:{},name:{}]", roleID,roleName);
	}
}

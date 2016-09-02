package com.generallycloud.nio.extend.configuration;

import java.util.ArrayList;
import java.util.List;

public class PermissionConfiguration {

	private List<Configuration>	roles		= new ArrayList<Configuration>();
	private List<Configuration>	permissions	= new ArrayList<Configuration>();

	public List<Configuration> getRoles() {
		return roles;
	}

	protected void addRole(Configuration role) {
		this.roles.add(role);
	}

	public List<Configuration> getPermissions() {
		return permissions;
	}

	protected void addPermission(Configuration permission) {
		this.permissions.add(permission);
	}

}

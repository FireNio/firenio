package com.generallycloud.nio.extend.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthorityManager {

	private Integer				roleID			;
	
	private Authority				authority			;

	private Map<String, Permission>	permissions		= new HashMap<String, Permission>();

	private List<Permission>			permissionsList	= new ArrayList<Permission>();

	protected void addPermission(Permission permission) {
		
		if (permissions.containsKey(permission.getPermissionAPI())) {
			return;
		}
		
		permissions.put(permission.getPermissionAPI(), permission);
		permissionsList.add(permission);
	}

	protected Integer getRoleID() {
		return roleID;
	}

	public boolean isInvokeApproved(String permissionAPI) {

		Permission permission = permissions.get(permissionAPI);

		return permission != null && permission.invoke();
	}

	protected void setRoleID(Integer roleID) {
		this.roleID = roleID;
	}

	protected AuthorityManager clone() {
		AuthorityManager manager = new AuthorityManager();

		manager.setRoleID(roleID);

		for (Permission p : permissionsList) {
			manager.addPermission(p.clone());
		}

		return manager;
	}
	
	protected void setAuthority(Authority authority){
		this.authority = authority;
	}
	
	public Authority getAuthority(){
		return authority;
	}

}

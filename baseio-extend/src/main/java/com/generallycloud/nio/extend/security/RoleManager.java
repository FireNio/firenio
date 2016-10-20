package com.generallycloud.nio.extend.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.generallycloud.nio.extend.ApplicationContext;
import com.generallycloud.nio.extend.InitializeableImpl;
import com.generallycloud.nio.extend.configuration.ApplicationConfiguration;
import com.generallycloud.nio.extend.configuration.Configuration;
import com.generallycloud.nio.extend.configuration.PermissionConfiguration;

public class RoleManager extends InitializeableImpl {

	private Map<Integer, AuthorityManager>	authorityManagers		= new HashMap<Integer, AuthorityManager>();

	private AuthorityManager				guestAuthorityManager	;

	public void initialize(ApplicationContext context, Configuration config) throws Exception {

		ApplicationConfiguration configuration = context.getConfiguration();

		PermissionConfiguration permissionConfiguration = configuration.getPermissionConfiguration();

		List<Configuration> permissionConfigurations = permissionConfiguration.getPermissions();

		List<Configuration> roleConfigurations = permissionConfiguration.getRoles();

		if (permissionConfigurations == null || permissionConfigurations.size() == 0 || roleConfigurations == null
				|| roleConfigurations.size() == 0) {
			throw new Error("没有加载到角色配置");
		}

		Map<Integer, Permission> permissions = new HashMap<Integer, Permission>();

		for (Configuration c : permissionConfigurations) {

			Permission p = new Permission();
			p.setDescription(c.getParameter("description"));
			p.setFrequency(c.getIntegerParameter("frequency"));
			p.setPermissionAPI(c.getParameter("permissionAPI"));
			p.setPermissionID(c.getIntegerParameter("permissionID"));

			permissions.put(p.getPermissionID(), p);
		}

		Map<Integer, Role> roles = new HashMap<Integer, Role>();
		List<Role> roleList = new ArrayList<Role>();

		for (Configuration c : roleConfigurations) {

			Role r = new Role();
			r.setDescription(c.getParameter("description"));
			r.setRoleID(c.getIntegerParameter("roleID"));
			r.setRoleName(c.getParameter("roleName"));

			JSONArray array = c.getJSONArray("children");

			if (array != null && !array.isEmpty()) {

				List<Integer> _children = new ArrayList<Integer>();

				for (int i = 0; i < array.size(); i++) {

					_children.add(array.getInteger(i));
				}

				r.setChildren(_children);
			}

			array = c.getJSONArray("permissions");

			if (array != null && !array.isEmpty()) {

				List<Integer> _permissions = new ArrayList<Integer>();

				for (int i = 0; i < array.size(); i++) {

					_permissions.add(array.getInteger(i));
				}

				r.setPermissions(_permissions);
			}

			roles.put(r.getRoleID(), r);
			roleList.add(r);
		}

		reflectPermission(roleList, roles, permissions);
	}

	private void reflectPermission(List<Role> roleList, Map<Integer, Role> roles, Map<Integer, Permission> permissions) {

		for (Role r : roleList) {

			AuthorityManager authorityManager = new AuthorityManager();

			authorityManager.setRoleID(r.getRoleID());

			reflectPermission(r, roles, permissions, authorityManager);

			authorityManagers.put(authorityManager.getRoleID(), authorityManager);
			
			if ("guest".equals(r.getRoleName())) {
				guestAuthorityManager = authorityManager;
			}
		}
	}

	private void reflectPermission(Role role, Map<Integer, Role> roles, Map<Integer, Permission> permissions,
			AuthorityManager authorityManager) {

		List<Integer> children = role.getChildren();

		if (children != null) {
			for (Integer rID : children) {

				Role r = roles.get(rID);

				if (r != null) {
					reflectPermission(r, roles, permissions, authorityManager);
				}
			}
		}

		List<Integer> _permissions = role.getPermissions();

		if (_permissions != null) {

			for (Integer pID : _permissions) {

				Permission p = permissions.get(pID);

				if (p != null) {
					authorityManager.addPermission(p);
				}
			}
		}
	}

	public AuthorityManager getAuthorityManager(Authority authority) {
		
		Integer roleID = authority.getRoleID();
		
		AuthorityManager authorityManager = authorityManagers.get(roleID);

		if (authorityManager == null) {
			authorityManager = guestAuthorityManager;
		}

		authorityManager = authorityManager.clone();
		
		authorityManager.setAuthority(authority);
		
		return authorityManager;
	}

	public static void main(String[] args) {

		System.out.println("[");
		for (int j = 0; j < 100; j++) {

			System.out.println("\t{");
			System.out.println("\t\t\"permissionID\": " + j + ",");
			System.out.println("\t\t\"permissionAPI\": \"\"");
			System.out.println("\t},");
		}
		System.out.println("]");

	}

}

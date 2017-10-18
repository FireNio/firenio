/*
 * Copyright 2015-2017 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.generallycloud.baseio.container.authority;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.generallycloud.baseio.collection.IntObjectHashMap;
import com.generallycloud.baseio.container.AbstractInitializeable;
import com.generallycloud.baseio.container.ApplicationContext;
import com.generallycloud.baseio.container.configuration.ApplicationConfigurationLoader;
import com.generallycloud.baseio.container.configuration.Configuration;
import com.generallycloud.baseio.container.configuration.PermissionConfiguration;

public class RoleManager extends AbstractInitializeable {

    private IntObjectHashMap<AuthorityManager> authorityManagers = new IntObjectHashMap<>();

    private AuthorityManager                   guestAuthorityManager;

    @Override
    public void initialize(ApplicationContext context, Configuration config) throws Exception {

        ApplicationConfigurationLoader acLoader = context.getAcLoader();

        PermissionConfiguration permissionConfiguration = acLoader
                .loadPermissionConfiguration(getClass().getClassLoader());

        List<Configuration> permissionConfigurations = permissionConfiguration.getPermissions();

        List<Configuration> roleConfigurations = permissionConfiguration.getRoles();

        if (permissionConfigurations == null || permissionConfigurations.size() == 0
                || roleConfigurations == null || roleConfigurations.size() == 0) {
            throw new Error("没有加载到角色配置");
        }

        IntObjectHashMap<Permission> permissions = new IntObjectHashMap<>();

        for (Configuration c : permissionConfigurations) {

            Permission p = new Permission();
            p.setDescription(c.getParameter("description"));
            p.setFrequency(c.getIntegerParameter("frequency"));
            p.setPermissionAPI(c.getParameter("permissionAPI"));
            p.setPermissionId(c.getIntegerParameter("permissionId"));

            permissions.put(p.getPermissionId(), p);
        }

        IntObjectHashMap<Role> roles = new IntObjectHashMap<>();
        List<Role> roleList = new ArrayList<>();

        for (Configuration c : roleConfigurations) {

            Role r = new Role();
            r.setDescription(c.getParameter("description"));
            r.setRoleId(c.getIntegerParameter("roleId"));
            r.setRoleName(c.getParameter("roleName"));

            JSONArray array = c.getJSONArray("children");

            if (array != null && !array.isEmpty()) {

                List<Integer> _children = new ArrayList<>();

                for (int i = 0; i < array.size(); i++) {

                    _children.add(array.getInteger(i));
                }

                r.setChildren(_children);
            }

            array = c.getJSONArray("permissions");

            if (array != null && !array.isEmpty()) {

                List<Integer> _permissions = new ArrayList<>();

                for (int i = 0; i < array.size(); i++) {

                    _permissions.add(array.getInteger(i));
                }

                r.setPermissions(_permissions);
            }

            roles.put(r.getRoleId(), r);
            roleList.add(r);
        }

        reflectPermission(roleList, roles, permissions);
    }

    private void reflectPermission(List<Role> roleList, IntObjectHashMap<Role> roles,
            IntObjectHashMap<Permission> permissions) {

        for (Role r : roleList) {

            AuthorityManager authorityManager = new AuthorityManager();

            authorityManager.setRoleId(r.getRoleId());

            reflectPermission(r, roles, permissions, authorityManager);

            authorityManagers.put(authorityManager.getRoleId(), authorityManager);
        }

        guestAuthorityManager = new AuthorityManager();

        guestAuthorityManager.setRoleId(Authority.GUEST.getRoleId());

        guestAuthorityManager.setAuthority(Authority.GUEST);

        authorityManagers.put(guestAuthorityManager.getRoleId(), guestAuthorityManager);

    }

    private void reflectPermission(Role role, IntObjectHashMap<Role> roles,
            IntObjectHashMap<Permission> permissions, AuthorityManager authorityManager) {

        List<Integer> children = role.getChildren();

        if (children != null) {
            for (Integer rId : children) {

                Role r = roles.get(rId);

                if (r != null) {
                    reflectPermission(r, roles, permissions, authorityManager);
                }
            }
        }

        List<Integer> _permissions = role.getPermissions();

        if (_permissions != null) {

            for (Integer pId : _permissions) {

                Permission p = permissions.get(pId);

                if (p != null) {
                    authorityManager.addPermission(p);
                }
            }
        }
    }

    public AuthorityManager getAuthorityManager(Authority authority) {

        int roleId = authority.getRoleId();

        AuthorityManager authorityManager = authorityManagers.get(roleId);

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
            System.out.println("\t\t\"permissionId\": " + j + ",");
            System.out.println("\t\t\"permissionAPI\": \"\"");
            System.out.println("\t},");
        }
        System.out.println("]");

    }

}

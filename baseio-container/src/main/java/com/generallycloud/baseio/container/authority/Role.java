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

import java.util.List;

import com.generallycloud.baseio.common.MessageFormatter;

public class Role {

    private int           roleId;
    private String        roleName;
    private String        description;
    private List<Integer> children;
    private List<Integer> permissions;

    public int getRoleId() {
        return roleId;
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

    protected void setRoleId(int roleId) {
        this.roleId = roleId;
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

    @Override
    public String toString() {
        return MessageFormatter.format("[id:{},name:{}]", roleId, roleName);
    }
}

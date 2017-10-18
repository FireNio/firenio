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

import com.generallycloud.baseio.common.MessageFormatter;

public class Permission {

    private int    permissionId;

    private String permissionAPI;

    private String description;

    private int    invoked;

    // 按分钟计算
    private int frequency = 60;

    protected void setFrequency(int frequency) {
        if (frequency < 1) {
            return;
        }
        this.frequency = frequency;
    }

    private long nextSection;

    public int getPermissionId() {
        return permissionId;
    }

    public String getPermissionAPI() {
        return permissionAPI;
    }

    public String getDescription() {
        return description;
    }

    protected void setPermissionId(int permissionId) {
        this.permissionId = permissionId;
    }

    protected void setPermissionAPI(String permissionAPI) {
        this.permissionAPI = permissionAPI;
    }

    protected void setDescription(String description) {
        this.description = description;
    }

    public boolean invoke() {

        long currentTimeMillis = System.currentTimeMillis();

        if (currentTimeMillis < nextSection) {

            return invoked++ < frequency;

        } else {

            nextSection = currentTimeMillis + 60 * 1000;

            invoked++;

            return true;
        }
    }

    @Override
    public Permission clone() {
        Permission p = new Permission();

        p.description = description;
        p.frequency = frequency;
        p.permissionAPI = permissionAPI;
        p.permissionId = permissionId;

        return p;
    }

    @Override
    public String toString() {
        return MessageFormatter.format("[id:{},api:{}]", permissionId, permissionAPI);
    }

}

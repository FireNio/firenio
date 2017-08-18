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
package com.generallycloud.baseio.container.configuration;

import java.io.IOException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.generallycloud.baseio.common.Encoding;
import com.generallycloud.baseio.common.FileUtil;
import com.generallycloud.baseio.common.StringUtil;

public class FileSystemACLoader extends AbstractACLoader {

    @Override
    protected FiltersConfiguration loadFiltersConfiguration(ClassLoader classLoader)
            throws IOException {

        String json = FileUtil.input2String(classLoader.getResourceAsStream("filters.cfg"),
                Encoding.UTF8);

        return loadFiltersConfiguration(json);
    }

    @Override
    protected PluginsConfiguration loadPluginsConfiguration(ClassLoader classLoader)
            throws IOException {

        String json = FileUtil.input2String(classLoader.getResourceAsStream("plugins.cfg"),
                Encoding.UTF8);

        return loadPluginsConfiguration(json);
    }

    @Override
    protected ServicesConfiguration loadServletsConfiguration(ClassLoader classLoader)
            throws IOException {

        String json = FileUtil.input2String(classLoader.getResourceAsStream("services.cfg"),
                Encoding.UTF8);

        return loadServletsConfiguration(json);
    }

    @Override
    public PermissionConfiguration loadPermissionConfiguration(ClassLoader classLoader)
            throws IOException {

        String roles = FileUtil.input2String(classLoader.getResourceAsStream("roles.cfg"),
                Encoding.UTF8);

        String permissions = FileUtil
                .input2String(classLoader.getResourceAsStream("permissions.cfg"), Encoding.UTF8);

        if (StringUtil.isNullOrBlank(roles) || StringUtil.isNullOrBlank(permissions)) {
            return null;
        }

        PermissionConfiguration configuration = new PermissionConfiguration();

        JSONArray array = JSON.parseArray(permissions);

        for (int i = 0; i < array.size(); i++) {

            Configuration permission = new Configuration(array.getJSONObject(i));

            configuration.addPermission(permission);
        }

        array = JSON.parseArray(roles);

        for (int i = 0; i < array.size(); i++) {

            Configuration role = new Configuration(array.getJSONObject(i));

            configuration.addRole(role);
        }

        return configuration;
    }

}

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
import com.generallycloud.baseio.common.Properties;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.configuration.PropertiesSCLoader;

public abstract class AbstractACLoader implements ApplicationConfigurationLoader {

    @Override
    public ApplicationConfiguration loadConfiguration(ClassLoader classLoader) throws Exception {

        ApplicationConfiguration configuration = new ApplicationConfiguration();
        
        Properties properties = FileUtil.readPropertiesByCls("app.properties", Encoding.UTF8, classLoader);
        
        new PropertiesSCLoader("APP").loadConfiguration(configuration, properties);

        configuration.setFiltersConfiguration(loadFiltersConfiguration(classLoader));
        configuration.setPluginsConfiguration(loadPluginsConfiguration(classLoader));
        configuration.setServletsConfiguration(loadServletsConfiguration(classLoader));

        return configuration;
    }

    protected abstract FiltersConfiguration loadFiltersConfiguration(ClassLoader classLoader)
            throws IOException;

    protected abstract PluginsConfiguration loadPluginsConfiguration(ClassLoader classLoader)
            throws IOException;

    protected abstract ServicesConfiguration loadServletsConfiguration(ClassLoader classLoader)
            throws IOException;

    protected FiltersConfiguration loadFiltersConfiguration(String json) {

        if (StringUtil.isNullOrBlank(json)) {
            return null;
        }

        JSONArray array = JSON.parseArray(json);

        FiltersConfiguration configuration = new FiltersConfiguration();

        for (int i = 0; i < array.size(); i++) {

            Configuration c = new Configuration(array.getJSONObject(i));

            configuration.addFilters(c);

        }

        return configuration;
    }

    protected PluginsConfiguration loadPluginsConfiguration(String json) {
        if (StringUtil.isNullOrBlank(json)) {
            return null;
        }
        JSONArray array = JSON.parseArray(json);
        PluginsConfiguration configuration = new PluginsConfiguration();
        for (int i = 0; i < array.size(); i++) {
            Configuration c = new Configuration(array.getJSONObject(i));
            configuration.addPlugin(c);
        }
        return configuration;
    }

    protected ServicesConfiguration loadServletsConfiguration(String json) {
        if (StringUtil.isNullOrBlank(json)) {
            return null;
        }
        JSONArray array = JSON.parseArray(json);
        ServicesConfiguration configuration = new ServicesConfiguration();
        for (int i = 0; i < array.size(); i++) {
            configuration.addService(new Configuration(array.getJSONObject(i)));
        }
        return configuration;
    }
}

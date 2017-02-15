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
package com.generallycloud.nio.container.configuration;

import java.io.IOException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.generallycloud.nio.common.Encoding;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.common.StringUtil;

public class FileSystemACLoader extends AbstractACLoader implements ApplicationConfigurationLoader {

	public FileSystemACLoader(String conf_path) {
		this.conf_path = conf_path;
	}

	private String	conf_path	= "conf/";

	@Override
	protected FiltersConfiguration loadFiltersConfiguration(SharedBundle bundle) throws IOException {

		String json = bundle.loadContent(conf_path + "filters.cfg", Encoding.UTF8);

		return loadFiltersConfiguration(json);
	}

	@Override
	protected PluginsConfiguration loadPluginsConfiguration(SharedBundle bundle) throws IOException {

		String json = bundle.loadContent(conf_path + "plugins.cfg", Encoding.UTF8);

		return loadPluginsConfiguration(json);
	}

	@Override
	protected ServicesConfiguration loadServletsConfiguration(SharedBundle bundle) throws IOException {

		String json = bundle.loadContent(conf_path + "services.cfg", Encoding.UTF8);

		return loadServletsConfiguration(json);
	}

	@Override
	protected PermissionConfiguration loadPermissionConfiguration(SharedBundle bundle) throws IOException {

		String roles = bundle.loadContent(conf_path + "roles.cfg", Encoding.UTF8);

		String permissions = bundle.loadContent(conf_path + "permissions.cfg", Encoding.UTF8);

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

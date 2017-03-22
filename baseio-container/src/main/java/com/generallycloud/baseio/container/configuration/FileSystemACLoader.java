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
import com.generallycloud.baseio.common.SharedBundle;
import com.generallycloud.baseio.common.StringUtil;

public class FileSystemACLoader extends AbstractACLoader implements ApplicationConfigurationLoader {
	
	private String					applicationRootPath;

	public FileSystemACLoader(String applicationRootPath) {
		this.applicationRootPath = applicationRootPath;
	}
	
	@Override
	protected void initApplicationConfigurationLoader(ApplicationConfiguration configuration) throws IOException {
		
		SharedBundle bundle = SharedBundle.instance();
		
		if (StringUtil.isNullOrBlank(applicationRootPath)) {
			configuration.setApplicationRootPath(bundle.getClassPath());
			return;
		}
		
		bundle.loadAllProperties(applicationRootPath);
		
		configuration.setApplicationRootPath(FileUtil.getPrettyPath(applicationRootPath));
	}

	@Override
	protected FiltersConfiguration loadFiltersConfiguration(SharedBundle bundle)
			throws IOException {

		String json = bundle.loadContent("conf/filters.cfg", Encoding.UTF8);

		return loadFiltersConfiguration(json);
	}

	@Override
	protected PluginsConfiguration loadPluginsConfiguration(SharedBundle bundle)
			throws IOException {

		String json = bundle.loadContent("conf/plugins.cfg", Encoding.UTF8);

		return loadPluginsConfiguration(json);
	}

	@Override
	protected ServicesConfiguration loadServletsConfiguration(SharedBundle bundle)
			throws IOException {

		String json = bundle.loadContent("conf/services.cfg", Encoding.UTF8);

		return loadServletsConfiguration(json);
	}

	@Override
	protected PermissionConfiguration loadPermissionConfiguration(SharedBundle bundle)
			throws IOException {

		String roles = bundle.loadContent("conf/roles.cfg", Encoding.UTF8);

		String permissions = bundle.loadContent("conf/permissions.cfg", Encoding.UTF8);

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

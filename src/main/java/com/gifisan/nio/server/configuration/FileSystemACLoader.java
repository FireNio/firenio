package com.gifisan.nio.server.configuration;

import java.io.IOException;

import com.alibaba.fastjson.JSONArray;
import com.gifisan.nio.Encoding;
import com.gifisan.nio.common.PropertiesLoader;
import com.gifisan.nio.common.SharedBundle;
import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.component.Configuration;

public class FileSystemACLoader extends AbstractACLoader implements ApplicationConfigurationLoader {

	protected FiltersConfiguration loadFiltersConfiguration(SharedBundle bundle) throws IOException {

		String json = PropertiesLoader.loadContent("filters.config", Encoding.UTF8);

		return loadFiltersConfiguration(json);
	}

	protected PluginsConfiguration loadPluginsConfiguration(SharedBundle bundle) throws IOException {

		String json = PropertiesLoader.loadContent("plugins.config", Encoding.UTF8);

		return loadPluginsConfiguration(json);
	}

	protected ServletsConfiguration loadServletsConfiguration(SharedBundle bundle) throws IOException {

		String json = PropertiesLoader.loadContent("servlets.config", Encoding.UTF8);

		return loadServletsConfiguration(json);
	}

	protected PermissionConfiguration loadPermissionConfiguration(SharedBundle bundle) throws IOException {

		String roles = PropertiesLoader.loadContent("permission/roles.config", Encoding.UTF8);

		String permissions = PropertiesLoader.loadContent("permission/permissions.config", Encoding.UTF8);

		if (StringUtil.isNullOrBlank(roles) || StringUtil.isNullOrBlank(permissions)) {
			return null;
		}
		
		PermissionConfiguration configuration = new PermissionConfiguration();

		JSONArray array = JSONArray.parseArray(permissions);

		for (int i = 0; i < array.size(); i++) {

			Configuration permission = new Configuration(array.getJSONObject(i));
			
			configuration.addPermission(permission);
		}
		
		array = JSONArray.parseArray(roles);
		
		
		for (int i = 0; i < array.size(); i++) {

			Configuration role = new Configuration(array.getJSONObject(i));
			
			configuration.addRole(role);
		}

		return configuration;
	}

}

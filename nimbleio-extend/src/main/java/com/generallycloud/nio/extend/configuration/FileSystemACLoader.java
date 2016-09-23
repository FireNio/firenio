package com.generallycloud.nio.extend.configuration;

import java.io.IOException;

import com.alibaba.fastjson.JSONArray;
import com.generallycloud.nio.Encoding;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.common.StringUtil;

public class FileSystemACLoader extends AbstractACLoader implements ApplicationConfigurationLoader {

	public FileSystemACLoader(String conf_path) {
		this.conf_path = conf_path;
	}

	private String	conf_path	= "conf/";

	protected FiltersConfiguration loadFiltersConfiguration(SharedBundle bundle) throws IOException {

		String json = bundle.loadContent(conf_path + "filters.cfg", Encoding.UTF8);

		return loadFiltersConfiguration(json);
	}

	protected PluginsConfiguration loadPluginsConfiguration(SharedBundle bundle) throws IOException {

		String json = bundle.loadContent(conf_path + "plugins.cfg", Encoding.UTF8);

		return loadPluginsConfiguration(json);
	}

	protected ServicesConfiguration loadServletsConfiguration(SharedBundle bundle) throws IOException {

		String json = bundle.loadContent(conf_path + "services.cfg", Encoding.UTF8);

		return loadServletsConfiguration(json);
	}

	protected PermissionConfiguration loadPermissionConfiguration(SharedBundle bundle) throws IOException {

		String roles = bundle.loadContent(conf_path + "roles.cfg", Encoding.UTF8);

		String permissions = bundle.loadContent(conf_path + "permissions.cfg", Encoding.UTF8);

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

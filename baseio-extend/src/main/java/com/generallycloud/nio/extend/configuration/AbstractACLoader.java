package com.generallycloud.nio.extend.configuration;

import java.io.IOException;

import com.alibaba.fastjson.JSONArray;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.common.StringUtil;

public abstract class AbstractACLoader implements ApplicationConfigurationLoader{

	public ApplicationConfiguration loadConfiguration(SharedBundle bundle) throws Exception {
		
		ApplicationConfiguration configuration = new ApplicationConfiguration();

		configuration.setFiltersConfiguration(loadFiltersConfiguration(bundle));
		configuration.setPluginsConfiguration(loadPluginsConfiguration(bundle));
		configuration.setServletsConfiguration(loadServletsConfiguration(bundle));
		configuration.setPermissionConfiguration(loadPermissionConfiguration(bundle));
		
		return configuration;
	}
	
	protected abstract FiltersConfiguration loadFiltersConfiguration(SharedBundle bundle) throws IOException;
	
	protected abstract PluginsConfiguration loadPluginsConfiguration(SharedBundle bundle) throws IOException;
	
	protected abstract ServicesConfiguration loadServletsConfiguration(SharedBundle bundle) throws IOException;
	
	protected abstract PermissionConfiguration loadPermissionConfiguration(SharedBundle bundle) throws IOException;
	
	protected FiltersConfiguration loadFiltersConfiguration(String json){
		
		if (StringUtil.isNullOrBlank(json)) {
			return null;
		}
		
		JSONArray array = JSONArray.parseArray(json);
		
		FiltersConfiguration configuration = new FiltersConfiguration();
		
		for (int i = 0; i < array.size(); i++) {
			
			Configuration c = new Configuration(array.getJSONObject(i));
			
			configuration.addFilters(c);
			
		}
		
		return configuration;
	}
	
	protected PluginsConfiguration loadPluginsConfiguration(String json){
		
		if (StringUtil.isNullOrBlank(json)) {
			return null;
		}
		
		JSONArray array = JSONArray.parseArray(json);
		
		PluginsConfiguration configuration = new PluginsConfiguration();
		
		for (int i = 0; i < array.size(); i++) {
			
			Configuration c = new Configuration(array.getJSONObject(i));
			
			configuration.addPlugins(c);
			
		}
		
		return configuration;
	}
	
	protected ServicesConfiguration loadServletsConfiguration(String json){
		
		if (StringUtil.isNullOrBlank(json)) {
			return null;
		}
		
		JSONArray array = JSONArray.parseArray(json);
		
		ServicesConfiguration configuration = new ServicesConfiguration();
		
		for (int i = 0; i < array.size(); i++) {
			
			Configuration c = new Configuration(array.getJSONObject(i));
			
			configuration.addServlets(c);
			
		}
		
		return configuration;
	}
}

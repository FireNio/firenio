package com.gifisan.nio.server.configuration;

import java.io.IOException;
import java.nio.charset.Charset;

import com.alibaba.fastjson.JSONArray;
import com.gifisan.nio.common.SharedBundle;
import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.component.Configuration;

public abstract class AbstractACLoader implements ApplicationConfigurationLoader{

	public ApplicationConfiguration loadConfiguration(SharedBundle bundle) throws Exception {
		
		ApplicationConfiguration configuration = new ApplicationConfiguration();

		configuration.setFiltersConfiguration(loadFiltersConfiguration(bundle));
		configuration.setPluginsConfiguration(loadPluginsConfiguration(bundle));
		configuration.setServerConfiguration(loadServerConfiguration(bundle));
		configuration.setServletsConfiguration(loadServletsConfiguration(bundle));
		
		return configuration;
	}
	
	protected ServerConfiguration loadServerConfiguration(SharedBundle bundle){
		
		ServerConfiguration configuration = new ServerConfiguration();
		
		configuration.setSERVER_CORE_SIZE(bundle.getIntegerProperty("SERVER.CORE_SIZE",4));
		configuration.setSERVER_DEBUG(bundle.getBooleanProperty("SERVER.DEBUG"));
		configuration.setSERVER_PASSWORD(bundle.getProperty("SERVER.PASSWORD", "admin100"));
		configuration.setSERVER_PORT(bundle.getIntegerProperty("SERVER.PORT"));
		configuration.setSERVER_UDP_BOOT(bundle.getBooleanProperty("SERVER.UDP_BOOT"));
		configuration.setSERVER_USERNAME(bundle.getProperty("SERVER.USERNAME", "admin"));

		String encoding = bundle.getProperty("SERVER.ENCODING", "GBK");
		Charset charset = Charset.forName(encoding);
		
		configuration.setSERVER_ENCODING(charset);

		return configuration;
	}
	
	
	protected abstract FiltersConfiguration loadFiltersConfiguration(SharedBundle bundle) throws IOException;
	
	protected abstract PluginsConfiguration loadPluginsConfiguration(SharedBundle bundle) throws IOException;
	
	protected abstract ServletsConfiguration loadServletsConfiguration(SharedBundle bundle) throws IOException;
	
	
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
	
	protected ServletsConfiguration loadServletsConfiguration(String json){
		
		if (StringUtil.isNullOrBlank(json)) {
			return null;
		}
		
		JSONArray array = JSONArray.parseArray(json);
		
		ServletsConfiguration configuration = new ServletsConfiguration();
		
		for (int i = 0; i < array.size(); i++) {
			
			Configuration c = new Configuration(array.getJSONObject(i));
			
			configuration.addServlets(c);
			
		}
		
		return configuration;
		
	}
}

package com.gifisan.nio.server.configuration;

import java.io.IOException;

import com.gifisan.nio.Encoding;
import com.gifisan.nio.common.PropertiesLoader;
import com.gifisan.nio.common.SharedBundle;

public class FileSystemACLoader extends AbstractACLoader implements ApplicationConfigurationLoader{

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
}

package com.generallycloud.nio.extend.configuration;

import java.util.ArrayList;
import java.util.List;

public class PluginsConfiguration {

	private List<Configuration> plugins = new ArrayList<Configuration>();

	public List<Configuration> getPlugins() {
		return plugins;
	}

	protected void addPlugins(Configuration plugin) {
		this.plugins.add(plugin);
	}
	
	
}

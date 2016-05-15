package com.gifisan.nio.server.configuration;

import java.util.ArrayList;
import java.util.List;

import com.gifisan.nio.component.Configuration;

public class PluginsConfiguration {

	private List<Configuration> plugins = new ArrayList<Configuration>();

	public List<Configuration> getPlugins() {
		return plugins;
	}

	protected void addPlugins(Configuration plugin) {
		this.plugins.add(plugin);
	}
	
	
}

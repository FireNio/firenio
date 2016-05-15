package com.gifisan.nio.server.configuration;


public class ApplicationConfiguration {

	private ServerConfiguration serverConfiguration = null;
	
	private FiltersConfiguration filtersConfiguration = null;
	
	private PluginsConfiguration pluginsConfiguration = null;
	
	private ServletsConfiguration servletsConfiguration = null;

	public ServerConfiguration getServerConfiguration() {
		return serverConfiguration;
	}

	protected void setServerConfiguration(ServerConfiguration serverConfiguration) {
		this.serverConfiguration = serverConfiguration;
	}

	public FiltersConfiguration getFiltersConfiguration() {
		return filtersConfiguration;
	}

	protected void setFiltersConfiguration(FiltersConfiguration filtersConfiguration) {
		this.filtersConfiguration = filtersConfiguration;
	}

	public PluginsConfiguration getPluginsConfiguration() {
		return pluginsConfiguration;
	}

	protected void setPluginsConfiguration(PluginsConfiguration pluginsConfiguration) {
		this.pluginsConfiguration = pluginsConfiguration;
	}

	public ServletsConfiguration getServletsConfiguration() {
		return servletsConfiguration;
	}

	protected void setServletsConfiguration(ServletsConfiguration servletsConfiguration) {
		this.servletsConfiguration = servletsConfiguration;
	}
	
}

package com.gifisan.nio.server.configuration;

public class ApplicationConfiguration {

	private FiltersConfiguration		filtersConfiguration	= null;

	private PermissionConfiguration	permissionConfiguration	= null;

	private PluginsConfiguration		pluginsConfiguration	= null;

	private ServerConfiguration		serverConfiguration		= null;

	private ServletsConfiguration		servletsConfiguration	= null;

	public FiltersConfiguration getFiltersConfiguration() {
		return filtersConfiguration;
	}

	public PermissionConfiguration getPermissionConfiguration() {
		return permissionConfiguration;
	}

	public PluginsConfiguration getPluginsConfiguration() {
		return pluginsConfiguration;
	}

	public ServerConfiguration getServerConfiguration() {
		return serverConfiguration;
	}

	public ServletsConfiguration getServletsConfiguration() {
		return servletsConfiguration;
	}

	protected void setFiltersConfiguration(FiltersConfiguration filtersConfiguration) {
		this.filtersConfiguration = filtersConfiguration;
	}

	protected void setPermissionConfiguration(PermissionConfiguration permissionConfiguration) {
		this.permissionConfiguration = permissionConfiguration;
	}

	protected void setPluginsConfiguration(PluginsConfiguration pluginsConfiguration) {
		this.pluginsConfiguration = pluginsConfiguration;
	}

	protected void setServerConfiguration(ServerConfiguration serverConfiguration) {
		this.serverConfiguration = serverConfiguration;
	}

	protected void setServletsConfiguration(ServletsConfiguration servletsConfiguration) {
		this.servletsConfiguration = servletsConfiguration;
	}

}

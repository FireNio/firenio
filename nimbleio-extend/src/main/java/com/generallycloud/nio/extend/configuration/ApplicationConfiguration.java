package com.generallycloud.nio.extend.configuration;

public class ApplicationConfiguration {

	private FiltersConfiguration		filtersConfiguration	;

	private PermissionConfiguration	permissionConfiguration	;

	private PluginsConfiguration		pluginsConfiguration	;

	private ServicesConfiguration		servletsConfiguration	;

	public FiltersConfiguration getFiltersConfiguration() {
		return filtersConfiguration;
	}

	public PermissionConfiguration getPermissionConfiguration() {
		return permissionConfiguration;
	}

	public PluginsConfiguration getPluginsConfiguration() {
		return pluginsConfiguration;
	}

	public ServicesConfiguration getServletsConfiguration() {
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

	protected void setServletsConfiguration(ServicesConfiguration servletsConfiguration) {
		this.servletsConfiguration = servletsConfiguration;
	}

}

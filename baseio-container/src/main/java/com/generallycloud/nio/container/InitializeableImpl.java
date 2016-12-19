package com.generallycloud.nio.container;

import com.generallycloud.nio.container.configuration.Configuration;



public abstract class InitializeableImpl implements Initializeable {

	private Configuration	config	;

	@Override
	public Configuration getConfig() {
		return this.config;
	}

	@Override
	public void setConfig(Configuration config) {
		this.config = config;
	}

	@Override
	public void destroy(ApplicationContext context, Configuration config) throws Exception {
	
	}
}

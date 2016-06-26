package com.gifisan.nio.extend;

import com.gifisan.nio.extend.configuration.Configuration;


public abstract class InitializeableImpl implements Initializeable {

	private Configuration	config	;

	public Configuration getConfig() {
		return this.config;
	}

	public void setConfig(Configuration config) {
		this.config = config;
	}

	public void destroy(ApplicationContext context, Configuration config) throws Exception {
	
	}
}

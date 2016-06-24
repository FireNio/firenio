package com.gifisan.nio.component;


public abstract class InitializeableImpl implements Initializeable {

	private Configuration	config	= null;

	public Configuration getConfig() {
		return this.config;
	}

	public void setConfig(Configuration config) {
		this.config = config;
	}

	public void destroy(ApplicationContext context, Configuration config) throws Exception {
	
	}
}

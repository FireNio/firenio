package com.gifisan.nio.component;

import com.gifisan.nio.server.NIOContext;

public abstract class InitializeableImpl implements Initializeable {

	private Configuration	config	= null;

	public Configuration getConfig() {
		return this.config;
	}

	public void setConfig(Configuration config) {
		this.config = config;
	}

	public void destroy(NIOContext context, Configuration config) throws Exception {
	}

}

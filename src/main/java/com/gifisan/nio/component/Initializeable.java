package com.gifisan.nio.component;


public interface Initializeable {

	public Configuration getConfig();

	public void setConfig(Configuration config);

	public abstract void initialize(ApplicationContext context, Configuration config) throws Exception;

	public abstract void destroy(ApplicationContext context, Configuration config) throws Exception;
	
}

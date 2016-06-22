package com.gifisan.nio.component;


public interface HotDeploy {

	public void prepare(ApplicationContext context, Configuration config) throws Exception;

	public void unload(ApplicationContext context, Configuration config) throws Exception;

}

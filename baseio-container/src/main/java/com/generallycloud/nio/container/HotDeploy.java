package com.generallycloud.nio.container;

import com.generallycloud.nio.container.configuration.Configuration;


public interface HotDeploy {

	public void prepare(ApplicationContext context, Configuration config) throws Exception;

	public void unload(ApplicationContext context, Configuration config) throws Exception;

}

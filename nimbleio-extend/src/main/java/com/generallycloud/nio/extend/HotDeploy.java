package com.generallycloud.nio.extend;

import com.generallycloud.nio.extend.configuration.Configuration;


public interface HotDeploy {

	public void prepare(ApplicationContext context, Configuration config) throws Exception;

	public void unload(ApplicationContext context, Configuration config) throws Exception;

}

package com.generallycloud.nio.extend;

import com.generallycloud.nio.component.Configuration;


public interface HotDeploy {

	public void prepare(ApplicationContext context, Configuration config) throws Exception;

	public void unload(ApplicationContext context, Configuration config) throws Exception;

}

package com.generallycloud.nio.configuration;

import com.generallycloud.nio.common.SharedBundle;

public interface ServerConfigurationLoader {
	
	public ServerConfiguration loadConfiguration(SharedBundle bundle) throws Exception;
}

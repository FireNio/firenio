package com.gifisan.nio.server.configuration;

import com.gifisan.nio.common.SharedBundle;

public interface ApplicationConfigurationLoader {

	public ApplicationConfiguration loadConfiguration(SharedBundle bundle) throws Exception;
	
}

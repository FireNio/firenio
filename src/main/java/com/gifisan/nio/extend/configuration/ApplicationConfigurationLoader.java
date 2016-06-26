package com.gifisan.nio.extend.configuration;

import com.gifisan.nio.common.SharedBundle;

public interface ApplicationConfigurationLoader {

	public ApplicationConfiguration loadConfiguration(SharedBundle bundle) throws Exception;
	
}

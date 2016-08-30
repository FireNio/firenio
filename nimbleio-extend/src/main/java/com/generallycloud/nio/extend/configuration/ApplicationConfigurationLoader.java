package com.generallycloud.nio.extend.configuration;

import com.generallycloud.nio.common.SharedBundle;

public interface ApplicationConfigurationLoader {

	public ApplicationConfiguration loadConfiguration(SharedBundle bundle) throws Exception;
	
}

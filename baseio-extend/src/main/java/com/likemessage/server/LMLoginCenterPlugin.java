package com.likemessage.server;

import com.generallycloud.nio.extend.AbstractPluginContext;
import com.generallycloud.nio.extend.ApplicationContext;
import com.generallycloud.nio.extend.configuration.Configuration;

public class LMLoginCenterPlugin extends AbstractPluginContext {

	public void initialize(ApplicationContext context, Configuration config) throws Exception {

		 context.setLoginCenter(new LMLoginCenter());
	}

}

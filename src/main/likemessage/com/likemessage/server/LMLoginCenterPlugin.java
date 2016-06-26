package com.likemessage.server;

import com.gifisan.nio.extend.AbstractPluginContext;
import com.gifisan.nio.extend.ApplicationContext;
import com.gifisan.nio.extend.configuration.Configuration;

public class LMLoginCenterPlugin extends AbstractPluginContext {

	public void initialize(ApplicationContext context, Configuration config) throws Exception {

		 context.setLoginCenter(new LMLoginCenter());
	}

}

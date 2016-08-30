package com.likemessage.server;

import com.generallycloud.nio.component.Configuration;
import com.generallycloud.nio.extend.AbstractPluginContext;
import com.generallycloud.nio.extend.ApplicationContext;

public class LMLoginCenterPlugin extends AbstractPluginContext {

	public void initialize(ApplicationContext context, Configuration config) throws Exception {

		 context.setLoginCenter(new LMLoginCenter());
	}

}

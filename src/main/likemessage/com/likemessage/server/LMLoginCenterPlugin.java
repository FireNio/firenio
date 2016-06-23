package com.likemessage.server;

import com.gifisan.nio.component.AbstractPluginContext;
import com.gifisan.nio.component.ApplicationContext;
import com.gifisan.nio.component.Configuration;

public class LMLoginCenterPlugin extends AbstractPluginContext {

	public void initialize(ApplicationContext context, Configuration config) throws Exception {

		 context.setLoginCenter(new LMLoginCenter());
	}

}

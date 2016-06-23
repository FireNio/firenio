package com.gifisan.nio.plugin.authority;

import java.util.Map;

import com.gifisan.nio.component.AbstractPluginContext;
import com.gifisan.nio.component.ApplicationContext;
import com.gifisan.nio.component.Configuration;
import com.gifisan.nio.server.service.GenericFutureAcceptor;
import com.likemessage.server.LMLoginCenter;

public class AuthorityPlugin extends AbstractPluginContext {
	
	private static AuthorityPlugin instance = null;
	
	public static AuthorityPlugin getInstance(){
		return instance;
	}

	public void configFutureAcceptor(Map<String, GenericFutureAcceptor> acceptors) {
		acceptors.put(SYSTEMAuthorityServlet.SERVICE_NAME, new SYSTEMAuthorityServlet());
	}

	public void initialize(ApplicationContext context, Configuration config) throws Exception {

		context.setLoginCenter(new LMLoginCenter());
		
		instance = this;
	}

}

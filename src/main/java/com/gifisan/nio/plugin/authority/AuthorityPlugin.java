package com.gifisan.nio.plugin.authority;

import java.util.List;
import java.util.Map;

import com.gifisan.nio.component.AbstractPluginContext;
import com.gifisan.nio.component.ApplicationContext;
import com.gifisan.nio.component.Configuration;
import com.gifisan.nio.server.service.GenericServlet;
import com.gifisan.nio.server.service.NIOFilter;
import com.likemessage.server.LMLoginCenter;

public class AuthorityPlugin extends AbstractPluginContext {
	
	private static AuthorityPlugin instance = null;
	
	public static AuthorityPlugin getInstance(){
		return instance;
	}

	public void configFilter(List<NIOFilter> pluginFilters) {

	}

	public void configServlet(Map<String, GenericServlet> servlets) {

	}

	public void initialize(ApplicationContext context, Configuration config) throws Exception {

		context.setLoginCenter(new LMLoginCenter());
		
		instance = this;
	}

}

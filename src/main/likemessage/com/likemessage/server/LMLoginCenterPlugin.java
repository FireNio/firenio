package com.likemessage.server;

import java.util.List;
import java.util.Map;

import com.gifisan.nio.component.AbstractPluginContext;
import com.gifisan.nio.component.ApplicationContext;
import com.gifisan.nio.component.Configuration;
import com.gifisan.nio.server.service.GenericFutureAcceptor;
import com.gifisan.nio.server.service.FutureAcceptorFilter;

public class LMLoginCenterPlugin extends AbstractPluginContext {

	public void configFilter(List<FutureAcceptorFilter> pluginFilters) {

	}

	public void configServlet(Map<String, GenericFutureAcceptor> servlets) {

	}

	public void initialize(ApplicationContext context, Configuration config) throws Exception {

		 context.setLoginCenter(new LMLoginCenter());
	}

}

package com.likemessage.server;

import java.util.List;
import java.util.Map;

import com.gifisan.nio.component.AbstractPluginContext;
import com.gifisan.nio.component.ApplicationContext;
import com.gifisan.nio.component.Configuration;
import com.gifisan.nio.server.service.GenericReadFutureAcceptor;
import com.gifisan.nio.server.service.ReadFutureAcceptorFilter;

public class LMLoginCenterPlugin extends AbstractPluginContext {

	public void configFilter(List<ReadFutureAcceptorFilter> pluginFilters) {

	}

	public void configServlet(Map<String, GenericReadFutureAcceptor> servlets) {

	}

	public void initialize(ApplicationContext context, Configuration config) throws Exception {

		 context.setLoginCenter(new LMLoginCenter());
	}

}

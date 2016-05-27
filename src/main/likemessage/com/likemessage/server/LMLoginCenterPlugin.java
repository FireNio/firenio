package com.likemessage.server;

import java.util.List;
import java.util.Map;

import com.gifisan.nio.component.AbstractPluginContext;
import com.gifisan.nio.component.Configuration;
import com.gifisan.nio.server.DefaultServerContext;
import com.gifisan.nio.server.ServerContext;
import com.gifisan.nio.server.service.GenericServlet;
import com.gifisan.nio.server.service.NIOFilter;

public class LMLoginCenterPlugin extends AbstractPluginContext {

	public void configFilter(List<NIOFilter> pluginFilters) {

	}

	public void configServlet(Map<String, GenericServlet> servlets) {

	}

	public void initialize(ServerContext context, Configuration config) throws Exception {

		((DefaultServerContext) context).setLoginCenter(new LMLoginCenter());
	}

}

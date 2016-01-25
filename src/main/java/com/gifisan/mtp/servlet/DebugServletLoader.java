package com.gifisan.mtp.servlet;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gifisan.mtp.component.DynamicClassLoader;
import com.gifisan.mtp.server.ServerContext;

public class DebugServletLoader extends NormalServletLoader implements ServletLoader {

	private final Logger				logger	= LoggerFactory.getLogger(DebugServletLoader.class);

	public DebugServletLoader(ServerContext context, DynamicClassLoader classLoader) {
		super(context, classLoader);
	}

	Map<String, GenericServlet> loadServlets(ServerContext context) throws Exception {
		return loadServlets(context, "servlets.config");
	}

	public boolean redeploy(DynamicClassLoader classLoader) {
		logger.warn("调试模式不支持热部署");
		return false;
	}

}

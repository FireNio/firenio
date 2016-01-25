package com.gifisan.mtp.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gifisan.mtp.component.DynamicClassLoader;
import com.gifisan.mtp.server.ServerContext;

public class DebugFilterLoader extends NormalFilterLoader implements FilterLoader {

	private Logger	logger	= LoggerFactory.getLogger(DebugFilterLoader.class);

	public DebugFilterLoader(ServerContext context, DynamicClassLoader classLoader) {
		super(context, classLoader);
	}

	void loadFilters(ServerContext context) {
		loadFilters(context, "filters.config");
	}

	public boolean redeploy(DynamicClassLoader classLoader) {
		logger.warn("调试模式不支持热部署");
		return false;
	}

}

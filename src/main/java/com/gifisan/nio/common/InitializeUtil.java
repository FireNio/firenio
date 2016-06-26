package com.gifisan.nio.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gifisan.nio.extend.ApplicationContext;
import com.gifisan.nio.extend.Initializeable;
import com.gifisan.nio.extend.configuration.Configuration;

public class InitializeUtil {

	private static Logger	logger	= LoggerFactory.getLogger(InitializeUtil.class);

	public static void destroy(Initializeable initializeable, ApplicationContext context, Configuration config) {

		if (initializeable == null) {
			return;
		}

		try {
			initializeable.destroy(context, config);
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}

	}
}

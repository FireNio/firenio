package com.gifisan.nio.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gifisan.nio.component.ApplicationContext;
import com.gifisan.nio.component.Configuration;
import com.gifisan.nio.component.Initializeable;

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

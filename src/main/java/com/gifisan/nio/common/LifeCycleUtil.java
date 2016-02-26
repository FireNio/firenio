package com.gifisan.nio.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gifisan.nio.LifeCycle;

public class LifeCycleUtil {

	private static Logger logger = LoggerFactory.getLogger(LifeCycleUtil.class);
	
	public static void stop(LifeCycle lifeCycle){
		if (lifeCycle == null) {
			return ;
		}
		try {
			lifeCycle.stop();
		} catch (Throwable e) {
			logger.error(e.getMessage(),e);
		}
	}
}

package com.gifisan.nio.common;

import com.gifisan.nio.LifeCycle;
import com.gifisan.nio.Stopable;

public class LifeCycleUtil {

	private static Logger	logger	= LoggerFactory.getLogger(LifeCycleUtil.class);

	public static void stop(LifeCycle lifeCycle) {
		if (lifeCycle == null) {
			return;
		}
		try {
			if (lifeCycle.isRunning()) {
				lifeCycle.stop();
			}
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
	}

	public static void start(LifeCycle lifeCycle){
		
		if (lifeCycle == null) {
			return;
		}
		
		if (!lifeCycle.isStopped()) {
			return;
		}
		
		try {
			lifeCycle.start();
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	public static void stop(Stopable stopable){
		if (stopable == null) {
			return;
		}
		try {
			stopable.stop();
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
	}

}

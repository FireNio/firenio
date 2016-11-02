package com.generallycloud.nio.common;

import com.generallycloud.nio.Releasable;

public class ReleaseUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(ReleaseUtil.class);

	public static void release(Releasable releasable){
		
		if (releasable == null) {
			return;
		}
		
		try {
			releasable.release();
		} catch (Throwable e) {
			logger.error(e.getMessage(),e);
		}
	}
}

package com.gifisan.nio.client;

import com.gifisan.nio.LifeCycle;
import com.gifisan.nio.common.DebugUtil;

public class ClientLifeCycleUtil {

	public static void stop(LifeCycle lifeCycle){
		if (lifeCycle == null) {
			return ;
		}
		try {
			lifeCycle.stop();
		} catch (Throwable e) {
			DebugUtil.debug(e);
		}
	}
}

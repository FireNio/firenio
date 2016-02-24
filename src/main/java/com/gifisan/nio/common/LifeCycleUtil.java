package com.gifisan.nio.common;

import com.gifisan.nio.LifeCycle;

public class LifeCycleUtil {

	public static void stop(LifeCycle lifeCycle){
		if (lifeCycle == null) {
			return ;
		}
		try {
			lifeCycle.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

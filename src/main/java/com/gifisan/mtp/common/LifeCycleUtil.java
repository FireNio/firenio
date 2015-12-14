package com.gifisan.mtp.common;

import com.gifisan.mtp.LifeCycle;

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

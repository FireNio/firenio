package com.gifisan.nio.common;

public class ThreadUtil {

	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			DebugUtil.debug(e);
		}
	}
}

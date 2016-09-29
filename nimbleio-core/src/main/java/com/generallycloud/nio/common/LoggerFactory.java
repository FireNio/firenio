package com.generallycloud.nio.common;


public class LoggerFactory {
	
	static{
		SharedBundle.instance();
	}
	
	private static boolean enableSLF4JLogger;
	
	public static void enableSLF4JLogger(boolean enable){
		enableSLF4JLogger = enable;
	}
	
	public static Logger getLogger(Class clazz){
		if (enableSLF4JLogger) {
			return new SLF4JLogger(clazz);
		}
		return new ConsoleLogger(clazz);
	}
	
	
}

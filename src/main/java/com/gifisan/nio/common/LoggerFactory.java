package com.gifisan.nio.common;

public class LoggerFactory {
	
	private static boolean enableSLF4JLogger = false;
	
	private static ConsoleLogger consoleLogger = new ConsoleLogger();

	public static void enableSLF4JLogger(boolean enable){
		enableSLF4JLogger = enable;
	}
	
	public static Logger getLogger(Class clazz){
		if (enableSLF4JLogger) {
			return new SLF4JLogger(org.slf4j.LoggerFactory.getLogger(clazz));
		}
		return consoleLogger;
	}
	
	
}

package com.generallycloud.nio.common;

import java.io.IOException;

public class LoggerFactory {
	
	static{
		try {
			PropertiesLoader.load();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			enableSLF4JLogger = false;
		}
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

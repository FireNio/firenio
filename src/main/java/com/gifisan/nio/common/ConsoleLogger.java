package com.gifisan.nio.common;



public class ConsoleLogger implements Logger{
	
	
	
	public void info(String message) {
		DebugUtil.info(message);
	}

	public void info(String message, Object param) {
		DebugUtil.info(message, param);
	}

	public void info(String message, Object param, Object param1) {
		DebugUtil.info(message, param, param1);
	}

	public void info(String message, Object[] param) {
		DebugUtil.info(message, param);
		
	}

	public void debug(String message) {
		DebugUtil.debug(message);
	}

	public void debug(String message, Object param) {
		DebugUtil.debug(message, param);
		
	}

	public void debug(String message, Object param, Object param1) {
		DebugUtil.debug(message, param, param1);		
	}

	public void debug(String message, Object[] param) {
		DebugUtil.debug(message, param);		
	}

	public void error(String object, Throwable throwable) {
		DebugUtil.error(object, throwable);		
	}
	
	public void error(String object) {
		DebugUtil.error(object);		
		
	}

	public static void main(String[] args) {
		new ConsoleLogger().info("test {}", "www");
		
	}
	

	
}

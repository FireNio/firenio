package com.generallycloud.nio.common;



public class ConsoleLogger implements Logger{
	
	private String className = null;
	
	private Class loggerClass = null;
	
	protected ConsoleLogger(Class clazz) {
		this.className = "["+clazz.getName()+"] ";
		this.loggerClass = clazz;
	}
	
	public Class getLoggerClass() {
		return loggerClass;
	}

	public void info(String message) {
		DebugUtil.info(className,message);
	}

	public void info(String message, Object param) {
		DebugUtil.info(className,message, param);
	}

	public void info(String message, Object param, Object param1) {
		DebugUtil.info(className,message, param, param1);
	}

	public void info(String message, Object[] param) {
		DebugUtil.info(className,message, param);
		
	}

	public void debug(String message) {
		DebugUtil.debug(className,message);
	}

	public void debug(String message, Object param) {
		DebugUtil.debug(className,message, param);
		
	}

	public void debug(String message, Object param, Object param1) {
		DebugUtil.debug(className,message, param, param1);		
	}

	public void debug(String message, Object[] param) {
		DebugUtil.debug(className,message, param);		
	}

	public void error(String object, Throwable throwable) {
		DebugUtil.error(className,object, throwable);		
	}
	
	public void error(String object) {
		DebugUtil.error(object);		
	}

	public void debug(Throwable throwable) {
		DebugUtil.debug(throwable);
	}

	public static void main(String[] args) {
		new ConsoleLogger(ConsoleLogger.class).info("test {}", "www");
	}
	

	
}

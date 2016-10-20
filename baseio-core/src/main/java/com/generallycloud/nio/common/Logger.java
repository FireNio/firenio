package com.generallycloud.nio.common;

public interface Logger {

	public abstract void info(String message);
	
	public abstract void info(String message,Object param);
	
	public abstract void info(String message,Object param,Object param1);
	
	public abstract void info(String message,Object []param);
	
	public abstract void debug(String message);
	
	public abstract void debug(Throwable throwable);
	
	public abstract void debug(String message,Object param);
	
	public abstract void debug(String message,Object param,Object param1);
	
	public abstract void debug(String message,Object []param);
	
	public abstract void error(String object);
	
	public abstract void error(String object,Throwable throwable); 
	
	public abstract Class getLoggerClass();
	
}

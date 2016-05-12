package com.gifisan.nio.common;

public class SLF4JLogger implements Logger{

	private org.slf4j.Logger logger = null;
	
	public SLF4JLogger(org.slf4j.Logger logger) {
		this.logger = logger;
	}

	public void info(String message) {
		logger.info(message);
	}

	public void info(String message, Object param) {
		logger.info(message, param);
		
	}

	public void info(String message, Object param, Object param1) {
		logger.info(message, param,param1);
		
	}

	public void info(String message, Object[] param) {
		logger.info(message, param);
		
	}

	public void debug(String message) {
		logger.debug(message);
		
	}

	public void debug(String message, Object param) {
		logger.debug(message, param);
		
	}

	public void debug(String message, Object param, Object param1) {
		logger.debug(message, param,param1);
	}

	public void debug(String message, Object[] param) {
		logger.debug(message, param);
	}

	public void error(String object, Throwable throwable) {
		logger.error(object,throwable);
	}

	public void error(String object) {
		logger.error(object);
	}
	
}

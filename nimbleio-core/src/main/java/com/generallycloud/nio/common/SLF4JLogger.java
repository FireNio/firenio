package com.generallycloud.nio.common;

import org.slf4j.LoggerFactory;

public class SLF4JLogger implements Logger{

	private org.slf4j.Logger logger = null;

	private Class loggerClass = null;
	
	public SLF4JLogger(Class clazz) {
		this.logger = LoggerFactory.getLogger(clazz);
		this.loggerClass = clazz;
	}
	
	public Class getLoggerClass() {
		return loggerClass;
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

	public void debug(Throwable throwable) {
		if (logger.isDebugEnabled()) {
			logger.error(throwable.getMessage(),throwable);
		}
	}
	
}

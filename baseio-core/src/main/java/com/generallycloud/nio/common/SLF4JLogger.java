package com.generallycloud.nio.common;

import org.slf4j.LoggerFactory;

public class SLF4JLogger implements Logger{

	private org.slf4j.Logger logger = null;

	private Class<?> loggerClass = null;
	
	public SLF4JLogger(Class<?> clazz) {
		this.logger = LoggerFactory.getLogger(clazz);
		this.loggerClass = clazz;
	}
	
	@Override
	public Class<?> getLoggerClass() {
		return loggerClass;
	}

	@Override
	public void info(String message) {
		logger.info(message);
	}

	@Override
	public void info(String message, Object param) {
		logger.info(message, param);
		
	}

	@Override
	public void info(String message, Object param, Object param1) {
		logger.info(message, param,param1);
		
	}

	@Override
	public void info(String message, Object[] param) {
		logger.info(message, param);
		
	}

	@Override
	public void debug(String message) {
		logger.debug(message);
		
	}

	@Override
	public void debug(String message, Object param) {
		logger.debug(message, param);
		
	}

	@Override
	public void debug(String message, Object param, Object param1) {
		logger.debug(message, param,param1);
	}

	@Override
	public void debug(String message, Object[] param) {
		logger.debug(message, param);
	}

	@Override
	public void error(String object, Throwable throwable) {
		logger.error(object,throwable);
	}

	@Override
	public void error(String object) {
		logger.error(object);
	}

	@Override
	public void debug(Throwable throwable) {
		if (logger.isDebugEnabled()) {
			logger.error(throwable.getMessage(),throwable);
		}
	}
	
}

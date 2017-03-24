/*
 * Copyright 2015-2017 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package com.generallycloud.baseio.common;



public class ConsoleLogger implements Logger{
	
	private String className = null;
	
	private Class<?> loggerClass = null;
	
	protected ConsoleLogger(Class<?> clazz) {
		this.className = "["+clazz.getName()+"] ";
		this.loggerClass = clazz;
	}
	
	@Override
	public Class<?> getLoggerClass() {
		return loggerClass;
	}

	@Override
	public void info(String message) {
		DebugUtil.info(className,message);
	}

	@Override
	public void info(String message, Object param) {
		DebugUtil.info(className,message, param);
	}

	@Override
	public void info(String message, Object param, Object param1) {
		DebugUtil.info(className,message, param, param1);
	}

	@Override
	public void info(String message, Object[] param) {
		DebugUtil.info(className,message, param);
	}

	@Override
	public void debug(String message) {
		DebugUtil.debug(className,message);
	}

	@Override
	public void debug(String message, Object param) {
		DebugUtil.debug(className,message, param);
		
	}

	@Override
	public void debug(String message, Object param, Object param1) {
		DebugUtil.debug(className,message, param, param1);		
	}

	@Override
	public void debug(String message, Object[] param) {
		DebugUtil.debug(className,message, param);		
	}

	@Override
	public void error(String object, Throwable throwable) {
		DebugUtil.error(className,object, throwable);		
	}
	
	@Override
	public void error(String object) {
		DebugUtil.error(object);		
	}

	@Override
	public void debug(Throwable throwable) {
		DebugUtil.debug(throwable);
	}

	public static void main(String[] args) {
		new ConsoleLogger(ConsoleLogger.class).info("test {}", "www");
	}

	@Override
	public void errorDebug(Throwable throwable) {
		errorDebug(throwable.getMessage(), throwable);
	}

	@Override
	public void errorDebug(String message, Throwable throwable) {
		DebugUtil.error(throwable.getMessage());
		DebugUtil.debug(throwable);
	}
	
}

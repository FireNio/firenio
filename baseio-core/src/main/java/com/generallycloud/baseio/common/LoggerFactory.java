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

public class LoggerFactory {

	private static boolean enableSLF4JLogger = false;
	
	static{
		configure();
	}

	public static void enableSLF4JLogger(boolean enable) {
		enableSLF4JLogger = enable;
	}

	public static Logger getLogger(Class<?> clazz) {
		if (!enableSLF4JLogger) {
			return new ConsoleLogger(clazz);
		}
		return new SLF4JLogger(clazz);
	}

	public static void configure() {
		try {
			ClassLoader cl = LoggerFactory.class.getClassLoader(); 
			cl.loadClass("org.slf4j.LoggerFactory");
			enableSLF4JLogger = true;
//			loadClass(cl,"org.apache.log4j.Appender");
//			loadClass(cl,"org.apache.log4j.spi.OptionHandler");
//			loadClass(cl,"org.apache.log4j.AppenderSkeleton");
//			loadClass(cl,"org.apache.log4j.WriterAppender");
//			loadClass(cl,"org.apache.log4j.FileAppender");
//			loadClass(cl,"org.apache.log4j.DailyRollingFileAppender");
//			loadClass(cl,"org.apache.log4j.ConsoleAppender");
		} catch (ClassNotFoundException e) {
		}
	}
	
	private static void loadClass(ClassLoader cl,String clazz){
		try {
			cl.loadClass(clazz);
		} catch (Throwable e) {
		}
	}
	
	public static boolean enableSLF4JLogger(){
		return enableSLF4JLogger;
	}
}

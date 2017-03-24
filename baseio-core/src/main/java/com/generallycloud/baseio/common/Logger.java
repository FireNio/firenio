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
	
	public abstract Class<?> getLoggerClass();
	
	public abstract void errorDebug(Throwable throwable);
	
	public abstract void errorDebug(String message,Throwable throwable);
	
}

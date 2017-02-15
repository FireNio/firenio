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
package com.generallycloud.nio.common;

//FIXME char util ?
public class ByteUtil {
	
	public static char			FALSE		= 'F';
	public static char			TRUE			= 'T';
	
	public static char getByte(boolean bool){
		return bool ? TRUE :FALSE;
	}

	public static boolean getBoolean(byte b){
		return TRUE == b;
	}
	
	public static boolean isTrue(String text){
		return "T".equals(text);
	}
	
	public static boolean isFalse(String text){
		return "F".equals(text);
	}
}

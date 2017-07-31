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

import java.util.Date;

public class DebugUtil {

	private static String	debugClassName;
	
	private static boolean enableDebug = false;

	private static String	errorClassName;

	private static String	infoClassName;

	static {
		String className = DebugUtil.class.getSimpleName() + " -";
		debugClassName = " [DEBUG] " + className;
		infoClassName  = " [INFO] " + className;
		errorClassName = " [ERROR] " + className;
	}

	public static void debug(String message) {
		if (enableDebug) {
			info(debugClassName, message);
		}
	}

	public static void debug(String message, Object param) {
		if (enableDebug) {
			info(debugClassName, message, param);
		}
	}

	public static void debug(String message, Object param, Object param1) {
		if (enableDebug) {
			info(debugClassName, message, param, param1);
		}
	}

	public static void debug(String message, Object[] param) {
		if (enableDebug) {
			info(debugClassName, message, param);
		}
	}

	public static void debug(String className, String message) {
		if (enableDebug) {
			info(className, message);
		}
	}

	public static void debug(String className, String message, Object param) {
		if (enableDebug) {
			info(className, message, param);
		}
	}

	public static void debug(String className, String message, Object param, Object param1) {
		if (enableDebug) {
			info(className, message, param, param1);
		}
	}

	public static void debug(String className, String message, Object[] param) {
		if (enableDebug) {
			info(className, message, param);
		}
	}

	public static void debug(Throwable e) {
		if (enableDebug) {
			printStackTrace(e);
		}
	}

	public static void error(String className, String message) {
		System.err.println(getTimeFormat() + className + message);
	}

	public static void error(String className, String message, Throwable e) {
		error(className, message);
		printStackTrace(e);
	}

	public static void error(Throwable e) {
		printStackTrace(e);
	}
	
	public static void error1(String message) {
		error(errorClassName, message);
	}

	public static void error1(String message, Throwable e) {
		error(errorClassName, message);
		printStackTrace(e);
	}

	public static String exception2string(Throwable exception) {
		StackTraceElement[] es = exception.getStackTrace();
		StringBuilder builder = new StringBuilder();
		builder.append(exception.toString());
		for (StackTraceElement e : es) {
			builder.append("\n\tat ");
			builder.append(e.toString());
		}
		return builder.toString();
	}
	
	private static String getTimeFormat() {
		return DateUtil.yyyy_MM_dd_HH_mm_ss_SSS.format(new Date());
	}
	
	public static void info(String className, String message) {
		System.out.println(getTimeFormat() + className + message);
	}

	public static void info(String className, String message, Object param) {
		System.out.println(getTimeFormat() + className + MessageFormatter.format(message, param));
	}

	public static void info(String className, String message, Object param, Object param1) {
		System.out.println(
				getTimeFormat() + className + MessageFormatter.format(message, param, param1));
	}

	public static void info(String className, String message, Object[] param) {
		System.out.println(
				getTimeFormat() + className + MessageFormatter.arrayFormat(message, param));
	}

	public static void info1(String message) {
		info(infoClassName, message);
	}

	public static void info1(String message, Object param) {
		info(infoClassName, message, param);
	}

	public static void info1(String message, Object param, Object param1) {
		info(infoClassName, message, param, param1);
	}

	public static void info1(String message, Object[] param) {
		info(infoClassName, message, param);
	}

	public static void printStackTrace(Throwable t) {
		t.printStackTrace(System.err);
	}
	
	public static void setEnableDebug(boolean enable) {
		enableDebug = enable;
	}

}

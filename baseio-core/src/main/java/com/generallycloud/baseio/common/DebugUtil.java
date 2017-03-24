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

public class DebugUtil {

	private static boolean enableDebug = false;

	public static void debug(Throwable e) {
		if (enableDebug) {
			printStackTrace(e);
		}
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

	public static void main(String[] args) {
		Exception e = new Exception("999999");
		printStackTrace(e);
		String msg = exception2string(e);
		System.out.println(msg);
	}

	public static void setEnableDebug(boolean enable) {
		enableDebug = enable;
	}

	public static void debug(String className, String message) {
		if (enableDebug) {
			System.out.println(className + ":" + message);
		}
	}

	public static void info(String className, String message) {
		System.out.println(className + message);
	}

	public static void info(String className, String message, Object param) {
		System.out.println(className + MessageFormatter.format(message, param));
	}

	public static void info(String className, String message, Object param, Object param1) {
		System.out.println(className + MessageFormatter.format(message, param, param1));
	}

	public static void info(String className, String message, Object[] param) {
		System.out.println(className + MessageFormatter.arrayFormat(message, param));

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

	public static void error(String className, String message, Throwable e) {
		if (message != null) {
			System.err.println(message);
		}
		printStackTrace(e);
	}

	public static void error(String message) {
		if (message != null) {
			System.err.println(message);
		}
	}

	public static void debug(String message) {
		if (enableDebug) {
			System.out.println(message);
		}
	}

	public static void info(String message) {
		System.out.println(message);
	}

	public static void info(String message, Object param) {
		System.out.println(MessageFormatter.format(message, param));
	}

	public static void info(String message, Object param, Object param1) {
		System.out.println(MessageFormatter.format(message, param, param1));
	}

	public static void info(String message, Object[] param) {
		System.out.println(MessageFormatter.arrayFormat(message, param));
	}

	public static void debug(String message, Object param) {
		if (enableDebug) {
			info(message, param);
		}
	}

	public static void debug(String message, Object param, Object param1) {
		if (enableDebug) {
			info(message, param, param1);
		}
	}

	public static void debug(String message, Object[] param) {
		if (enableDebug) {
			info(message, param);
		}
	}

	public static void error(String message, Throwable e) {
		if (message != null) {
			System.err.println(message);
		}
		printStackTrace(e);
	}

	public static void error(Throwable e) {
		printStackTrace(e);
	}

	public static void printStackTrace(Throwable t) {
		t.printStackTrace();
	}

}

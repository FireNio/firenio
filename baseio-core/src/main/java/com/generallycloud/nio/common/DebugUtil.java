package com.generallycloud.nio.common;

public class DebugUtil {

	private static boolean	enableDebug	= true;

	protected static void debug(Throwable e) {
		if (enableDebug) {
			e.printStackTrace();
		}
	}

	protected static String exception2string(Throwable exception) {
		StackTraceElement[] es = exception.getStackTrace();
		StringBuilder builder = new StringBuilder();
		builder.append(exception.toString());
		for (StackTraceElement e : es) {
			builder.append("\n\tat ");
			builder.append(e.toString());
		}
		return builder.toString();
	}

	protected static void main(String[] args) {
		Exception e = new Exception("999999");
		e.printStackTrace();
		String msg = exception2string(e);
		System.out.println(msg);
	}

	public static void setEnableDebug(boolean enable) {
		enableDebug = enable;
	}

	protected static void debug(String className, String message) {
		if (enableDebug) {
			System.out.println(className + "===" + message);
		}
	}

	protected static void info(String className, String message) {
		System.out.println(className + message);
	}

	protected static void info(String className, String message, Object param) {

		System.out.println(className + MessageFormatter.format(message, param));
	}

	protected static void info(String className, String message, Object param, Object param1) {
		System.out.println(className + MessageFormatter.format(message, param, param1));

	}

	protected static void info(String className, String message, Object[] param) {
		System.out.println(className + MessageFormatter.arrayFormat(message, param));

	}

	protected static void debug(String className, String message, Object param) {
		if (enableDebug) {
			info(className, message, param);
		}

	}

	protected static void debug(String className, String message, Object param, Object param1) {
		if (enableDebug) {
			info(className, message, param, param1);
		}

	}

	protected static void debug(String className, String message, Object[] param) {
		if (enableDebug) {
			info(className, message, param);
		}
	}

	protected static void error(String className, String message, Throwable throwable) {
		if (message != null) {
			System.err.println(message);
		}
		throwable.printStackTrace();
	}

	protected static void error(String message) {
		if (message != null) {
			System.err.println(message);
		}
	}

}

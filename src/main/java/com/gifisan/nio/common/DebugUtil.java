package com.gifisan.nio.common;

public class DebugUtil {

	private static boolean	enableDebug	= true;

	public static void debug(Throwable e) {
		if (enableDebug) {
			e.printStackTrace();
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
		e.printStackTrace();
		String msg = exception2string(e);
		System.out.println(msg);
	}

	public static void setEnableDebug(boolean enable) {
		enableDebug = enable;
	}

	public static void debug(String className, String message) {
		if (enableDebug) {
			System.out.println(className + "===" + message);
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

	public static void error(String className, String message, Throwable throwable) {
		System.err.println(message);
		throwable.printStackTrace();
	}

	public static void error(String messsage) {
		System.err.println(messsage);
	}

}

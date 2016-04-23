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

	public static void error(String object, Throwable throwable) {
		System.out.println(object);
		throwable.printStackTrace();
	}

}

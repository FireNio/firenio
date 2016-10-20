package com.generallycloud.nio.common;


public class LoggerUtil {

	private static int	maxNameLength	= "FutureAcceptorServiceLoader".length();

	private static String getSpace(Logger logger) {

		Class clazz = logger.getLoggerClass();

		String name = clazz.getSimpleName();

		int length = name.length();

		int _length = maxNameLength - length;

		if (_length == 0) {
			return "";
		}

		StringBuilder builder = new StringBuilder();

		for (; _length > 0; _length--) {
			builder.append(" ");
		}

		return builder.toString();
	}

	public static void prettyNIOServerLog(Logger logger, String msg) {

		msg = getSpace(logger) + "[NIOServer] " + msg;

		logger.info(msg);
	}

	public static void prettyNIOServerLog(Logger logger, String msg, Object param1) {

		msg = getSpace(logger) + "[NIOServer] " + msg;

		logger.info(msg, param1);
	}

	public static void prettyNIOServerLog(Logger logger, String msg, Object param1, Object param2) {
		msg = getSpace(logger) + "[NIOServer] " + msg;

		logger.info(msg, param1, param2);
	}

	public static void prettyNIOServerLog(Logger logger, String msg, Object[] param) {
		msg = getSpace(logger) + "[NIOServer] " + msg;

		logger.info(msg, param);
	}
}

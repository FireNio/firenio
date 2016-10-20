package com.generallycloud.nio.common;

import java.util.HashMap;
import java.util.Map;

final public class MessageFormatter {
	private static final char		DELIM_START	= '{';
	private static final String		DELIM_STR	= "{}";
	private static final char		ESCAPE_CHAR	= '\\';

	final public static String format(String messagePattern, Object arg) {
		return arrayFormat(messagePattern, new Object[] { arg });
	}

	final public static String format(final String messagePattern, Object arg1, Object arg2) {
		return arrayFormat(messagePattern, new Object[] { arg1, arg2 });
	}

	final public static String arrayFormat(final String messagePattern, final Object[] argArray) {

		if (messagePattern == null) {
			return null;
		}

		if (argArray == null) {
			return messagePattern;
		}

		int i = 0;
		int j;
		StringBuilder builder = new StringBuilder(messagePattern.length() + 50);

		int L;
		for (L = 0; L < argArray.length; L++) {

			j = messagePattern.indexOf(DELIM_STR, i);

			if (j == -1) {
				// no more variables
				if (i == 0) { // this is a simple string
					return messagePattern;
				} else { // add the tail string which contains no variables
						// and return
					// the result.
					builder.append(messagePattern.substring(i, messagePattern.length()));
					return builder.toString();
				}
			} else {
				if (isEscapedDelimeter(messagePattern, j)) {
					if (!isDoubleEscaped(messagePattern, j)) {
						L--; // DELIM_START was escaped, thus should not
							// be incremented
						builder.append(messagePattern.substring(i, j - 1));
						builder.append(DELIM_START);
						i = j + 1;
					} else {
						// The escape character preceding the delimiter
						// start is
						// itself escaped: "abc x:\\{}"
						// we have to consume one backward slash
						builder.append(messagePattern.substring(i, j - 1));
						deeplyAppendParameter(builder, argArray[L], new HashMap());
						i = j + 2;
					}
				} else {
					// normal case
					builder.append(messagePattern.substring(i, j));
					deeplyAppendParameter(builder, argArray[L], new HashMap());
					i = j + 2;
				}
			}
		}
		// append the characters following the last {} pair.
		builder.append(messagePattern.substring(i, messagePattern.length()));
		if (L < argArray.length - 1) {
			return builder.toString();
		} else {
			return builder.toString();
		}
	}

	private final static boolean isEscapedDelimeter(String messagePattern, int delimeterStartIndex) {

		if (delimeterStartIndex == 0) {
			return false;
		}
		char potentialEscape = messagePattern.charAt(delimeterStartIndex - 1);
		if (potentialEscape == ESCAPE_CHAR) {
			return true;
		} else {
			return false;
		}
	}

	private final static boolean isDoubleEscaped(String messagePattern, int delimeterStartIndex) {
		if (delimeterStartIndex >= 2 && messagePattern.charAt(delimeterStartIndex - 2) == ESCAPE_CHAR) {
			return true;
		} else {
			return false;
		}
	}

	// special treatment of array values was suggested by 'lizongbo'
	private static void deeplyAppendParameter(StringBuilder sbuf, Object o, Map seenMap) {
		if (o == null) {
			sbuf.append("null");
			return;
		}
		if (!o.getClass().isArray()) {
			safeObjectAppend(sbuf, o);
		} else {
			// check for primitive array types because they
			// unfortunately cannot be cast to Object[]
			if (o instanceof boolean[]) {
				booleanArrayAppend(sbuf, (boolean[]) o);
			} else if (o instanceof byte[]) {
				byteArrayAppend(sbuf, (byte[]) o);
			} else if (o instanceof char[]) {
				charArrayAppend(sbuf, (char[]) o);
			} else if (o instanceof short[]) {
				shortArrayAppend(sbuf, (short[]) o);
			} else if (o instanceof int[]) {
				intArrayAppend(sbuf, (int[]) o);
			} else if (o instanceof long[]) {
				longArrayAppend(sbuf, (long[]) o);
			} else if (o instanceof float[]) {
				floatArrayAppend(sbuf, (float[]) o);
			} else if (o instanceof double[]) {
				doubleArrayAppend(sbuf, (double[]) o);
			} else {
				objectArrayAppend(sbuf, (Object[]) o, seenMap);
			}
		}
	}

	private static void safeObjectAppend(StringBuilder sbuf, Object o) {
		try {
			String oAsString = o.toString();
			sbuf.append(oAsString);
		} catch (Throwable t) {
			System.err.println("SLF4J: Failed toString() invocation on an object of type [" + o.getClass().getName()
					+ "]");
			t.printStackTrace();
			sbuf.append("[FAILED toString()]");
		}

	}

	private static void objectArrayAppend(StringBuilder sbuf, Object[] a, Map seenMap) {
		sbuf.append('[');
		if (!seenMap.containsKey(a)) {
			seenMap.put(a, null);
			final int len = a.length;
			for (int i = 0; i < len; i++) {
				deeplyAppendParameter(sbuf, a[i], seenMap);
				if (i != len - 1)
					sbuf.append(", ");
			}
			// allow repeats in siblings
			seenMap.remove(a);
		} else {
			sbuf.append("...");
		}
		sbuf.append(']');
	}

	private static void booleanArrayAppend(StringBuilder sbuf, boolean[] a) {
		sbuf.append('[');
		final int len = a.length;
		for (int i = 0; i < len; i++) {
			sbuf.append(a[i]);
			if (i != len - 1)
				sbuf.append(", ");
		}
		sbuf.append(']');
	}

	private static void byteArrayAppend(StringBuilder sbuf, byte[] a) {
		sbuf.append('[');
		final int len = a.length;
		for (int i = 0; i < len; i++) {
			sbuf.append(a[i]);
			if (i != len - 1)
				sbuf.append(", ");
		}
		sbuf.append(']');
	}

	private static void charArrayAppend(StringBuilder sbuf, char[] a) {
		sbuf.append('[');
		final int len = a.length;
		for (int i = 0; i < len; i++) {
			sbuf.append(a[i]);
			if (i != len - 1)
				sbuf.append(", ");
		}
		sbuf.append(']');
	}

	private static void shortArrayAppend(StringBuilder sbuf, short[] a) {
		sbuf.append('[');
		final int len = a.length;
		for (int i = 0; i < len; i++) {
			sbuf.append(a[i]);
			if (i != len - 1)
				sbuf.append(", ");
		}
		sbuf.append(']');
	}

	private static void intArrayAppend(StringBuilder sbuf, int[] a) {
		sbuf.append('[');
		final int len = a.length;
		for (int i = 0; i < len; i++) {
			sbuf.append(a[i]);
			if (i != len - 1)
				sbuf.append(", ");
		}
		sbuf.append(']');
	}

	private static void longArrayAppend(StringBuilder sbuf, long[] a) {
		sbuf.append('[');
		final int len = a.length;
		for (int i = 0; i < len; i++) {
			sbuf.append(a[i]);
			if (i != len - 1)
				sbuf.append(", ");
		}
		sbuf.append(']');
	}

	private static void floatArrayAppend(StringBuilder sbuf, float[] a) {
		sbuf.append('[');
		final int len = a.length;
		for (int i = 0; i < len; i++) {
			sbuf.append(a[i]);
			if (i != len - 1)
				sbuf.append(", ");
		}
		sbuf.append(']');
	}

	private static void doubleArrayAppend(StringBuilder sbuf, double[] a) {
		sbuf.append('[');
		final int len = a.length;
		for (int i = 0; i < len; i++) {
			sbuf.append(a[i]);
			if (i != len - 1)
				sbuf.append(", ");
		}
		sbuf.append(']');
	}
}

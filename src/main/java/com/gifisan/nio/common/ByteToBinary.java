package com.gifisan.nio.common;

public class ByteToBinary {

	/**
	 * 把byte转化成2进制字符串
	 * 
	 * @param b
	 * @return
	 */
	public static String getBinaryStrFromByte(byte b) {
		StringBuilder builder = new StringBuilder();
		byte a = b;
		for (int i = 0; i < 8; i++) {
			byte c = a;
			a = (byte) (a >> 1);// 每移一位如同将10进制数除以2并去掉余数。
			a = (byte) (a << 1);
			if (a == c) {
				builder.append("0");
			} else {
				builder.append("1");
			}
			a = (byte) (a >> 1);
		}
		return builder.toString();
	}
}

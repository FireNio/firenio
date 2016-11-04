package com.generallycloud.nio.codec.http2;

import com.generallycloud.nio.common.MathUtil;

public class Test {

	public static void main(String[] args) {

		String pri = "PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n";

		byte[] array = pri.getBytes();

		String str = MathUtil.bytes2HexString(array);

		System.out.println(str);
		
		System.out.println(((byte)200) & 0xff);

	}
}

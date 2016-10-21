package com.generallycloud.test.nio.others;

import java.io.IOException;

import com.generallycloud.nio.common.MathUtil;

public class Test {

	
	public static final byte	PROTOCOL_RESPONSE			= 1;
	public static final byte	PROTOCOL_PUSH				= 2;
	public static final byte	PROTOCOL_BRODCAST			= 3;
	
	public static void main(String[] args) throws IOException {

		byte b = 127;
		
		System.out.println(MathUtil.byte2BinaryString(b));
		System.out.println(MathUtil.byte2BinaryString((byte)(b&0x3f)));
		
		System.out.println(MathUtil.byte2BinaryString((byte) -1));
		System.out.println(MathUtil.byte2BinaryString((byte) -2));
		
		System.out.println(Integer.MAX_VALUE >> 3);
		
		System.out.println(MathUtil.binaryString2HexString("00100000"));
		
		//test branch   tes22222
		
	}
}

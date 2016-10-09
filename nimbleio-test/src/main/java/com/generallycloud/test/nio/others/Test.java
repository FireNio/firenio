package com.generallycloud.test.nio.others;

import java.io.IOException;

import com.generallycloud.nio.common.MathUtil;

public class Test {

	public static void main(String[] args) throws IOException {

		byte b = 127;
		
		System.out.println(MathUtil.byte2BinaryString(b));
		System.out.println(MathUtil.byte2BinaryString((byte)(b&0x3f)));
		
		System.out.println(MathUtil.byte2BinaryString((byte) -1));
		System.out.println(MathUtil.byte2BinaryString((byte) -2));
		
		//test branch   tes22222
		
	}
}

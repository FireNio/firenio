package com.generallycloud.test.nio.others;

import com.generallycloud.nio.common.MathUtil;
import com.generallycloud.nio.common.test.ITest;
import com.generallycloud.nio.common.test.ITestHandle;

import static com.generallycloud.nio.common.MathUtil.*;

public class TestMathUtil {
	
	public static void main(String[] args) {

//		int time = 999999;

		final byte[] bb = new byte[10];

//		long value = 11111111112L;

//		long2Byte(bb, value, 0);

//		ITestHandle.doTest(new ITest() {
//			public void test(int i) throws Exception {
//				MathUtil.byte2Long(bb, 0);
//			}
//		}, time, "Byte2Long");

//		System.out.println(byte2Long(bb, 0));

//		System.out.println(bytes2HexString(new byte[] { 125, -22, -25, 89, 19, 90 }));

//		System.out.println(byte2BinaryString((byte) -127));
//		System.out.println(byte2BinaryString((byte) -128));
//		System.out.println(byte2BinaryString((byte) -2));
//		System.out.println(byte2BinaryString((byte) -1));
		
//		System.out.println(binaryString2HexString("00111111"));
//		System.out.println(binaryString2HexString("01111111"));
//		System.out.println(binaryString2HexString("10000000"));
		
		
		System.out.println(byte2BinaryString((byte) 0x3F));
		
		System.out.println();
	}
}

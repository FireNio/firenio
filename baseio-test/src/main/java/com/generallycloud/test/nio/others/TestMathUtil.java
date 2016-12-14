package com.generallycloud.test.nio.others;

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
		
		int v = Integer.MAX_VALUE >> 1;
		
		System.out.println(v);
		System.out.println(Integer.toBinaryString(v));
		System.out.println(byte2BinaryString((byte) 0x3F));
		System.out.println( (Integer.MAX_VALUE + 5) & 0x7fffffff);
		
		int s = 19810313;
		int2Byte(bb, s, 0);
		System.out.println();
		
		long l = 1;
		
		System.out.println(1 << 32);
		System.out.println(l << 32);
		
		
		System.out.println();
		
		
	}
}

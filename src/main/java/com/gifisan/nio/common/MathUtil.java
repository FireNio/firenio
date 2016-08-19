package com.gifisan.nio.common;

import com.gifisan.nio.common.test.ITest;
import com.gifisan.nio.common.test.ITestHandle;


public class MathUtil {
	
	public static int byte2Int(byte[] bytes) {
		int v0 = (bytes[3] & 0xff);
		int v1 = (bytes[2] & 0xff) << 8*1;
		int v2 = (bytes[2] & 0xff) << 8*2;
		int v3 = (bytes[0] & 0xff) << 8*3;
		return v0 | v1 | v2 | v3;
	}

	public static int byte2Int(byte[] bytes, int offset) {

		checkLength(bytes, 4, offset);

		int v0 = (bytes[offset + 3] & 0xff);
		int v1 = (bytes[offset + 2] & 0xff) << 8*1;
		int v2 = (bytes[offset + 1] & 0xff) << 8*2;
		int v3 = (bytes[offset + 0] & 0xff) << 8*3;
		return v0 | v1 | v2 | v3;

	}
	
	public static int byte2IntFrom2Byte(byte[] bytes, int offset) {

		checkLength(bytes, 2, offset);

		int v0 = (bytes[offset + 1] & 0xff);
		int v1 = (bytes[offset + 0] & 0xff) << 8*1;
		return v0 | v1;

	}

	private static void checkLength(byte[] bytes, int length, int offset) {

		if (bytes == null) {
			throw new IllegalArgumentException("null");
		}

		if (offset < 0) {
			throw new IllegalArgumentException("invalidate offset " + offset);
		}
		
		if (bytes.length - offset < length) {
			throw new IllegalArgumentException("invalidate length " + bytes.length);
		}
	}

	public static long byte2Long(byte[] bytes, int offset) {

		checkLength(bytes, 8, offset);

		long v0 = (long)(bytes[offset + 7] & 0xff);
		long v1 = (long)(bytes[offset + 6] & 0xff) << 8*1;
		long v2 = (long)(bytes[offset + 5] & 0xff) << 8*2;
		long v3 = (long)(bytes[offset + 4] & 0xff) << 8*3;
		long v4 = (long)(bytes[offset + 3] & 0xff) << 8*4;
		long v5 = (long)(bytes[offset + 2] & 0xff) << 8*5;
		long v6 = (long)(bytes[offset + 1] & 0xff) << 8*6;
		long v7 = (long)(bytes[offset + 0] & 0xff) << 8*7;
		return (v0 | v1 | v2 | v3 | v4 | v5 | v6 | v7);

	}

	public static long byte2Long(byte[] bytes) {

		long v0 = (long)(bytes[7] & 0xff);
		long v1 = (long)(bytes[6] & 0xff) << 8*1;
		long v2 = (long)(bytes[5] & 0xff) << 8*2;
		long v3 = (long)(bytes[4] & 0xff) << 8*3;
		long v4 = (long)(bytes[3] & 0xff) << 8*4;
		long v5 = (long)(bytes[2] & 0xff) << 8*5;
		long v6 = (long)(bytes[1] & 0xff) << 8*6;
		long v7 = (long)(bytes[0] & 0xff) << 8*7;
		return (v0 | v1 | v2 | v3 | v4 | v5 | v6 | v7);
	}
	
	public static void int2Byte(byte[] bytes, int value, int offset) {
		
		checkLength(bytes, 4, offset);

		bytes[offset + 3] = (byte) ((value & 0xff));
		bytes[offset + 2] = (byte) ((value >> 8*1) & 0xff);
		bytes[offset + 1] = (byte) ((value >> 8*2) & 0xff);
		bytes[offset + 0] = (byte) ((value >> 8*3));
	}
	
	public static byte[] int2Byte(int value) {
		
		byte[] bytes = new byte[4];

		bytes[3] = (byte) ((value & 0xff));
		bytes[2] = (byte) ((value >> 8*1) & 0xff);
		bytes[1] = (byte) ((value >> 8*2) & 0xff);
		bytes[0] = (byte) ((value >> 8*3));
		
		return bytes;
	}
	
	public static void int2Byte(byte [] bytes,int value) {
		bytes[3] = (byte) ((value & 0xff));
		bytes[2] = (byte) ((value >> 8*1) & 0xff);
		bytes[1] = (byte) ((value >> 8*2) & 0xff);
		bytes[0] = (byte) ((value >> 8*3));
	}
	
	public static void intTo2Byte(byte[] bytes, int value, int offset) {
		
		checkLength(bytes, 2, offset);

		bytes[offset + 1] = (byte) (value & 0xff);
		bytes[offset + 0] = (byte) (value >> 8*1);
	}
	
	public static byte[] long2Byte(long value) {
		
		byte[] bytes = new byte[8];

		bytes[7] = (byte) ((value & 0xff));
		bytes[6] = (byte) ((value >> 8*1) & 0xff);
		bytes[5] = (byte) ((value >> 8*2) & 0xff);
		bytes[4] = (byte) ((value >> 8*3) & 0xff);
		bytes[3] = (byte) ((value >> 8*4) & 0xff);
		bytes[2] = (byte) ((value >> 8*5) & 0xff);
		bytes[1] = (byte) ((value >> 8*6) & 0xff);
		bytes[0] = (byte) ((value >> 8*7));
		
		return bytes;
	}

	public static void long2Byte(byte[] bytes, long value, int offset) {
		
		checkLength(bytes, 8, offset);

		bytes[offset + 7] = (byte) ((value & 0xff));
		bytes[offset + 6] = (byte) ((value >> 8*1) & 0xff);
		bytes[offset + 5] = (byte) ((value >> 8*2) & 0xff);
		bytes[offset + 4] = (byte) ((value >> 8*3) & 0xff);
		bytes[offset + 3] = (byte) ((value >> 8*4) & 0xff);
		bytes[offset + 2] = (byte) ((value >> 8*5) & 0xff);
		bytes[offset + 1] = (byte) ((value >> 8*6) & 0xff);
		bytes[offset + 0] = (byte) ((value >> 8*7));

	}
	
	public static String getHexString(byte [] array){
		
		if (array == null || array.length == 0) {
			return null;
		}
		
		StringBuilder builder = new StringBuilder();
		
		builder.append("[");
		
		for (int i = 0; i < array.length; i++) {
			
			builder.append("0x");
			
			builder.append(getHexString(array[i]));
			
			builder.append(",");
		}
		
		builder.deleteCharAt(builder.length() - 1);
		
		builder.append("]");
		
		return builder.toString();
	}
	
	public static String getHexString(byte b){
		return Integer.toHexString(b & 0xFF);
	}
	
	public static int long2int(long value){
		return (int) (value % Integer.MAX_VALUE);
	}

	public static void main(String[] args) {

		int time = 1;
		
		final byte [] bb = new byte[10];
		
		long value = 11111111112L;
		
		long2Byte(bb, value, 0);

		ITestHandle.doTest(new ITest() {

			public void test() throws Exception {
				MathUtil.byte2Long(bb, 1);
			}
		}, time, "Byte2Long");

	
		System.out.println(byte2Long(bb, 0));
		
		System.out.println(getHexString(new byte[]{125}));
		
	}

}

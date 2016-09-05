package com.generallycloud.nio.common;

import com.generallycloud.nio.common.test.ITest;
import com.generallycloud.nio.common.test.ITestHandle;


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
			
			builder.append(byte2HexString(array[i]));
			
			builder.append(",");
		}
		
		builder.deleteCharAt(builder.length() - 1);
		
		builder.append("]");
		
		return builder.toString();
	}
	
	public static String byte2HexString(byte b){
		return Integer.toHexString(b & 0xFF);
	}
	
	public static int long2int(long value){
		return (int) (value % Integer.MAX_VALUE);
	}

	/**
	 * 右起0 ~ 7
	 * @param b
	 * @param pos
	 * @return
	 */
	public static boolean getBoolean(byte b ,int pos){
		if (pos < 0 || pos > 8) {
			throw new IllegalArgumentException("illegal pos,"+pos);
		}
		return (b & (1 << pos)) >> pos == 1;
	}
	
	public static String byte2BinaryString(byte b){
		StringBuilder builder = new StringBuilder();
		for (int i = 7; i > -1; i--) {
			builder.append(getBoolean(b, i) ? '1' : '0');
		}
		return builder.toString();
	}
	
	public static byte binaryString2byte(String string){
		
		Assert.notNull(string, "null binary string");
		char c0 = '0';
		char c1 = '1';
		if (string.length() != 8) {
			throw new IllegalArgumentException("except length 8");
		}
		char [] cs = string.toCharArray();
		byte result = 0;
		for (int i = 0; i < 8; i++) {
			char c = cs[i];
			int x = 0;
			if (c0 == c) {
			}else if(c1 ==c){
				x = 1;
			}else{
				throw new IllegalArgumentException(String.valueOf(c));
			}
			result = (byte) ((x << (7-i)) | result);
		}
		return result;
	}
	
	public static String binaryString2HexString(String string){
		byte b = binaryString2byte(string);
		return byte2HexString(b);
	}
	
	public static void main(String[] args) {

		int time = 999999;
		
		final byte [] bb = new byte[10];
		
		long value = 11111111112L;
		
		long2Byte(bb, value, 0);

		ITestHandle.doTest(new ITest() {

			public void test() throws Exception {
				MathUtil.byte2Long(bb, 1);
			}
		}, time, "Byte2Long");

	
		System.out.println(byte2Long(bb, 0));
		
		System.out.println(getHexString(new byte[]{125,-22,-25,89,19,90}));
		
		System.out.println(byte2BinaryString((byte)-127));
		System.out.println(byte2BinaryString((byte)-128));
		System.out.println(byte2BinaryString((byte)-2));
		System.out.println(byte2BinaryString((byte)-1));
	}

}

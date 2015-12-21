package com.gifisan;

public class Test {
	static int getPLength(byte [] header){
		int v0 = (header[0] );
	    int v1 = (header[1] ) << 8;   
//	    int v2 = (header[7] & 0xff) << 16;  
//	    return v0 | v1 | v2;
	    return v0 | v1;
	}
	
	public static void main(String[] args) {
		
		
		System.out.println(new Test().getClass().getName());
		
		System.out.println(1000 * 60 * 60 * 24 * 7);
		
		byte [] header = new byte[2];
		
		int number = 32767;
		
		header[0]  = (byte) ( number          & 0xff);
		header[1]  = (byte) ((number >>   8)  & 0xff);
		System.out.println(header[0]);
		System.out.println(header[1]);
		
		byte [] integer = new byte[]{127,127};
		
		int value = getPLength(integer);
		
		System.out.println(value);
		
		byte bb = 127;
		
		byte aa = -128;
		
		byte cc = -1;
		
		System.out.println(bb << 1);
		System.out.println(aa << 1);
		
		System.out.println(Integer.MAX_VALUE/(1024*1024));
		
		System.out.println(0xff);
		
		System.out.println(cc & 0xff);
		
		System.out.println(Integer.toBinaryString(-1));
		System.out.println(Integer.MIN_VALUE);
		System.out.println(Integer.MAX_VALUE+1);
		
		System.out.println(Integer.toBinaryString(17));
		System.out.println(Integer.toBinaryString(-17));
		System.out.println(Integer.MIN_VALUE-1);
		
		System.out.println(Integer.MAX_VALUE*2);
		
	}
}

package com.yoocent.mtp.common;

public class BITUtil {
	
	public static byte [] int2byte(int number){
		byte [] bytes = new byte[4];
		bytes[0] = (byte) ( number          & 0xff);// 最低位 
		bytes[1] = (byte) ((number >>   8)  & 0xff);// 次低位 
		bytes[2] = (byte) ((number >>  16)  & 0xff);// 次高位 
		bytes[3] = (byte) ( number >>> 24);         // 最高位,无符号右移。 
		return bytes;
	}
	
	public static int byte2int(byte[] bytes){
		int v0 = (bytes[0] & 0xff);      //&0xff将byte值无差异转成int,避免Java自动类型提升后,会保留高位的符号位  
	    int v1 = (bytes[1] & 0xff) << 8;  
	    int v2 = (bytes[2] & 0xff) << 16;  
	    int v3 = (bytes[3] & 0xff) << 24;  
	    return v0 | v1 | v2 | v3;
	}
	
	public static void main(String[] args) {
		
		byte [] bytes = int2byte(Integer.MAX_VALUE);
		
		int value = byte2int(bytes);
		
		System.out.println(value);
	}
	
	
}

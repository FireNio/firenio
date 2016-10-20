package com.generallycloud.nio.common;

public class ByteUtil {
	
	
	public static byte			FALSE		= 'F';
	public static byte			TRUE			= 'T';
	
	public static byte getByte(boolean bool){
		return bool ? TRUE :FALSE;
	}

	public static boolean getBoolean(byte b){
		return TRUE == b;
	}
	
	public static boolean isTrue(String text){
		return "T".equals(text);
	}
	
	public static boolean isFalse(String text){
		return "F".equals(text);
	}
}

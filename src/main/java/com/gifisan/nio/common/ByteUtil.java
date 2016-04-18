package com.gifisan.nio.common;

public class ByteUtil {
	
	
	public static byte			FALSE		= 'F';
	public static byte			TRUE			= 'T';
	
	public static byte getByte(boolean bool){
		return bool ? TRUE :FALSE;
	}

	public static boolean getBoolean(byte b){
		return TRUE == b;
	}
	
}

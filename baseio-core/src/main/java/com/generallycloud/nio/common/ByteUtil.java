package com.generallycloud.nio.common;

//FIXME char util ?
public class ByteUtil {
	
	
	public static char			FALSE		= 'F';
	public static char			TRUE			= 'T';
	
	public static char getByte(boolean bool){
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

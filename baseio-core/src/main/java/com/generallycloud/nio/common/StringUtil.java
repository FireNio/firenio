package com.generallycloud.nio.common;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

public class StringUtil {
	
	private static String [] zeros;
	
	static{
		int max = 15;
		int i = 0;
		zeros = new String[max+1];
		zeros[0] = "";
		for(; i++ < max;){
			zeros[i] = zeros[i-1] + "0";
		}
	}
	
	public static boolean isNullOrBlank(String value){
		
		return value == null || value.length() == 0;
	}

	public static boolean hasLength(String text) {
		
		return text != null && text.length() > 0;
	}

	public static boolean hasText(String text) {
		return text != null && text.trim().length() > 0;
	}
	
	public static String getZeroString(int length){
		return zeros[length];
	}
	
	//FIXME 尽量使用decode取代new String()
	//see  java.lang.StringCoding.decode(Charset cs, byte[] ba, int off, int len)
	public static String decode(Charset charset,ByteBuffer buffer){
		
		CharBuffer cb = charset.decode(buffer);
		
		return cb.toString();
	}
}

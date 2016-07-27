package com.gifisan.nio.common;

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
}
